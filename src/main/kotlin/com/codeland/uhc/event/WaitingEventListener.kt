package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.gui.GuiOpener
import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import com.destroystokyo.paper.utils.PaperPluginLogger
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin
import java.util.logging.Level


class WaitingEventListener() : Listener {

	@EventHandler
	fun onPlayerHurt(event : EntityDamageEvent) {
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING))
			return

		if (event.entityType == EntityType.PLAYER)
			event.isCancelled = true
	}

	@EventHandler
	fun onPlayerJoin(event : PlayerJoinEvent) {
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			if (GameRunner.playersTeam(event.player.name) == null)
				event.player.gameMode = GameMode.SPECTATOR

			return
		}

		val player = event.player
		val inventory = player.inventory

		/* get them on the health scoreboard */
		player.damage(1.0)
		player.gameMode = GameMode.ADVENTURE

		/* give them the gui opener */
		if (!GuiOpener.hasGuiOpener(inventory))
			inventory.addItem(GuiOpener.createGuiOpener())
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		/* only can open uhc settings while in waiting */
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING))
			return

		val stack = event.item
			?: return

		if (!GuiOpener.isGuiOpener(stack))
			return

		GameRunner.gui.open(event.player)
	}

	/**
	 * this is a better way of preventing hunger loss during waiting
	 * than a potion effect
	 */
	@EventHandler
	fun onHunger(event: FoodLevelChangeEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onWorldLoad(e : WorldLoadEvent) {
		e.world.setSpawnLocation(10000, 70, 10000)
		e.world.worldBorder.setCenter(10000.0, 10000.0)
		e.world.worldBorder.size = 50.0
		e.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		e.world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
		e.world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false) // could cause issue with dynamic spawn limit if true
		e.world.time = 1000
		e.world.difficulty = Difficulty.NORMAL
	}

	@EventHandler
	fun onPlayerDeath(event : PlayerDeathEvent) {
		event.entity.gameMode = GameMode.SPECTATOR
		GameRunner.playerDeath(event.entity)

		/* begin pest section */
		if (!GameRunner.pests.enabled)
			return

		var player = event.entity

		if (!Pests.isPest(player))
			return;

		/* don't drop anything */
		player.inventory.clear()
		event.setShouldDropExperience(false)
	}

	@EventHandler
	fun onMessage(e : AsyncPlayerChatEvent) {
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			if (!e.message.startsWith("!")) {
				val team = GameRunner.playersTeam(e.player.displayName)
				if (team != null) {
					e.isCancelled = true
					PaperPluginLogger.getGlobal().log(Level.INFO, "PLAYER SENT MESSAGE IN TEAM CHAT")
					for (entry in team.entries) {
						val precomp = TextComponent("<")
						val name = TextComponent(e.player.displayName)
						name.color = team.color.asBungee()
						name.isUnderlined = true
						val remaining = TextComponent("> ${e.message}")
						Bukkit.getPlayer(entry)?.sendMessage(precomp, name, remaining)
					}
				}
			} else {
				e.message = e.message.substring(1)
			}
		}
	}

	@EventHandler
	fun onPlayerTeleport(e : PlayerTeleportEvent) {
		if (!GameRunner.netherIsAllowed()) {
			if (e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && e.player.gameMode == GameMode.SURVIVAL) {
				e.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onEntitySpawn(event : EntitySpawnEvent) {
		/* prevent spawns during waiting */
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING))
			return

		if (event.entityType.isAlive)
			event.isCancelled = true
	}

	private val pestArmorMeta = {
		var meta = ItemStack(Material.LEATHER_HELMET).itemMeta

		meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)

		meta
	}()

	fun genPestArmor(item: Material): ItemStack {
		var stack = ItemStack(item)

		stack.itemMeta = pestArmorMeta;

		return stack
	}

	private val pestToolMeta = {
		var meta = ItemStack(Material.WOODEN_PICKAXE).itemMeta

		meta.isUnbreakable = true;

		meta
	}()

	fun genPestTool(item: Material): ItemStack {
		var stack = ItemStack(item)

		stack.itemMeta = pestToolMeta;

		return stack
	}

	@EventHandler
	fun onPlayerRespawn(event: PlayerRespawnEvent) {
		/* only do this on pests mode */
		if (!GameRunner.pests.enabled)
			return

		var player = event.player

		Pests.makePest(player)

		var border = player.world.worldBorder

		Bukkit.getServer().dispatchCommand(GameRunner.uhc.gameMaster!!, "spreadplayers ${border.center.x} ${border.center.z} 0 ${border.size / 2} true ${player.name}")

		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 2.0

		/* give pest a bunch of crap */
		player.inventory.helmet = genPestArmor(Material.LEATHER_HELMET)
		player.inventory.chestplate = genPestArmor(Material.LEATHER_CHESTPLATE)
		player.inventory.leggings = genPestArmor(Material.LEATHER_LEGGINGS)
		player.inventory.boots = genPestArmor(Material.LEATHER_BOOTS)

		player.inventory.setItem(0, genPestTool(Material.WOODEN_PICKAXE))
		player.inventory.setItem(1, genPestTool(Material.WOODEN_AXE))
		player.inventory.setItem(2, genPestTool(Material.WOODEN_SHOVEL))
		player.inventory.setItem(3, genPestTool(Material.WOODEN_HOE))
		player.inventory.setItem(4, genPestTool(Material.WOODEN_SWORD))
	}

	private val pestBanList = {
		val arr = arrayOf<Material>(
			Material.IRON_PICKAXE,
			Material.IRON_AXE,
			Material.IRON_HOE,
			Material.IRON_SHOVEL,
			Material.IRON_SWORD,
			Material.IRON_HELMET,
			Material.IRON_CHESTPLATE,
			Material.IRON_LEGGINGS,
			Material.IRON_BOOTS,
			Material.BOW,
			Material.SHIELD
		)

		arr.sort()

		arr
	}()

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		/* only do this on pests mode */
		if (!GameRunner.pests.enabled)
			return

		var player = event.target as Player;

		/* monsters will not target the pests */
		if (Pests.isPest(player))
			event.isCancelled = true;
	}

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		/* only do this on pests mode */
		if (!GameRunner.pests.enabled)
			return

		var player = event.whoClicked;

		if (!Pests.isPest(player as Player))
			return

		var item = event.recipe.result.type

		/* prevent crafting of banned items */
		if (binarySearch(item, pestBanList))
			event.isCancelled = true
	}

	/*@EventHandler
	fun onPlayerHold(e : PlayerItemHeldEvent) {
		if (GameRunner.uhc.graceType != GraceType.HALFZATOICHI) {
			return
		}
		if (e.player.inventory.itemInMainHand.type == Material.IRON_SWORD) {
			if (e.player.inventory.itemInMainHand.itemMeta.displayName == "Half Zatoichi") {
				e.isCancelled = true
			}
		}
	}*/

	@EventHandler
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		val stack = event.itemDrop.itemStack

		event.isCancelled = when {
			isHalfZatoichi(stack) -> true
			GuiOpener.isGuiOpener(stack) -> true
			else -> false
		}
	}

	@EventHandler
	fun onEntityDamageEvent(e : EntityDamageByEntityEvent) {
		if (GameRunner.halfZatoichi.enabled) {
			return
		}
		if (e.damager is Player) {
			val attacker = e.damager as Player
		}
		if (e.damager is Player && e.entity is Player) {
			val defender = e.entity as Player
			val attacker = e.damager as Player
			if (isHalfZatoichi(attacker.inventory.itemInMainHand)) {
				if (isHalfZatoichi(defender.inventory.itemInMainHand)) {
					defender.health = 0.0
					defender.absorptionAmount = 0.0
				}
				if (defender.health + defender.absorptionAmount < e.finalDamage) {
					if (attacker.health < 10.0) {
						attacker.health += 10.0
					} else {
						attacker.absorptionAmount += attacker.health - 10.0
						attacker.health = 20.0
					}
					val meta = attacker.inventory.itemInMainHand.itemMeta.clone()
					meta.setDisplayName("Half Zatoichi (bloody)")
					attacker.inventory.itemInMainHand.itemMeta = meta
					PaperPluginLogger.getGlobal().log(Level.INFO, "damaged entity")
				}
			}
		}
	}

	fun isHalfZatoichi(item : ItemStack?) : Boolean {
		if (item?.type == Material.IRON_SWORD) {
			if (item.itemMeta.displayName.startsWith("Half Zatoichi")) {
				return true
			}
		}
		return false
	}

	fun enchantThing(item : ItemStack, enchant : Enchantment, level : Int) {
		val meta = item.itemMeta
		meta.addEnchant(enchant, level, true)
		item.itemMeta = meta
	}

	@EventHandler
	fun onBreakBlock(event : BlockBreakEvent) {
		var block = event.block;
		var player = event.player;

		val getTool = {
			/* get what the player is holding */
			/* pretend it isn't air if it is */
			var tool = player.inventory.itemInMainHand;
			if (tool.type == Material.AIR)
				tool = ItemStack(Material.PORKCHOP);

			/* get drops from block as if held item */
			/* had fortune */
			var fakeTool = tool.clone();
			if (GameRunner.abundance.enabled) enchantThing(fakeTool, LOOT_BONUS_BLOCKS, 5);

			fakeTool;
		}

		/* these replace regular block breaking behavior */
		event.isCancelled = GameRunner.unsheltered.enabled || GameRunner.abundance.enabled;

		if (GameRunner.unsheltered.enabled) {
			/* regular block breaking behavior for acceptable blocks */
			if (binarySearch(block.type, acceptedBlocks)) {
				event.isCancelled = false
				return
			}

			var broken = block.state.getMetadata("broken")

			/* if we have not applied broken label or broken is explicitly set to false */
			/* proceed to mine then set broken to true */
			if (broken.size == 0 || !broken[0].asBoolean()) {
				/* manually drop items instead of the block breaking */
				var drops = block.getDrops(getTool())
				for (drop in drops)
					player.world.dropItem(block.location, ItemStack(drop.type, drop.amount))

				/* make sure we can't break this block again */
				block.state.setMetadata("broken", FixedMetadataValue(GameRunner.plugin as Plugin, true))
			} else {
				var message = TextComponent("Block already broken!")
				message.isBold = true
				message.color = ChatColor.GOLD

				player.sendMessage(message)
			}
		} else if (GameRunner.abundance.enabled) {
			block.breakNaturally(getTool())
		}
	}

	val acceptedBlocks = {
		var arr = arrayOf<Material>(
				Material.CRAFTING_TABLE,
				Material.FURNACE,
				Material.BREWING_STAND,
				Material.WHEAT_SEEDS,
				Material.BLAST_FURNACE,
				Material.SMOKER,
				Material.WATER,
				Material.LAVA,
				Material.LADDER,
				Material.ENCHANTING_TABLE,
				Material.BOOKSHELF,
				Material.SMITHING_TABLE,
				Material.LOOM,
				Material.ANVIL,
				Material.FLETCHING_TABLE,
				Material.COMPOSTER,
				Material.CHEST,
				Material.BARREL
		);
		arr.sort();

		arr;
	}();

	fun <T : Enum<T>>binarySearch(value: T, array: Array<T>): Boolean {
		var start = 0;
		var end = array.size - 1;
		var lookFor = value.ordinal;

		while (true) {
			var position = (end + start) / 2;
			var compare = array[position].ordinal;

			when {
				lookFor == compare -> return true;
				  end - start == 1 -> return false;
				 lookFor < compare -> end = position;
				 lookFor > compare -> start = position;
			}
		}
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		if (GameRunner.unsheltered.enabled) {
			var block = event.block;

			if (!binarySearch(block.type, acceptedBlocks)) {
				event.isCancelled = true;
			}
		}
	}
}
