package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiOpener
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.Pests
import com.codeland.uhc.quirk.Quirk
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
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING) && !GameRunner.uhc.isPhase(PhaseType.POSTGAME))
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

		Gui.open(event.player)
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
		var wasPest = Pests.isPest(event.entity)

		if (Quirk.PESTS.enabled) {
			if (event.entity.gameMode != GameMode.SPECTATOR)
				Pests.makePest(event.entity)

		} else {
			event.entity.gameMode = GameMode.SPECTATOR
		}

		if (!wasPest)
			GameRunner.playerDeath(event.entity)
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

	@EventHandler
	fun onPlayerRespawn(event: PlayerRespawnEvent) {
		/* only do this on pests mode */
		if (!Quirk.PESTS.enabled)
			return

		var player = event.player

		/* player is set to pest on death */
		if (!Pests.isPest(player))
			return

		var border = player.world.worldBorder

		/* spread player */
		var right = border.center.x + border.size / 2 - 10
		var down = border.center.z + border.size / 2 - 10

		var x = ((Math.random() * right * 2) - right).toInt()
		var z = ((Math.random() * down * 2) - down).toInt()

		var world = player.world

		for (y in 255 downTo 0) {
			var block = world.getBlockAt(x, y, z)

			if (!block.isPassable) {
				event.respawnLocation.set(x + 0.5, y + 1.0, z + 0.5)
				break
			}
		}

		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 4.0

		/* give pest a bunch of crap */
		player.inventory.helmet = Pests.genPestArmor(Material.LEATHER_HELMET)
		player.inventory.chestplate = Pests.genPestArmor(Material.LEATHER_CHESTPLATE)
		player.inventory.leggings = Pests.genPestArmor(Material.LEATHER_LEGGINGS)
		player.inventory.boots = Pests.genPestArmor(Material.LEATHER_BOOTS)

		player.inventory.setItem(0, Pests.genPestTool(Material.WOODEN_PICKAXE))
		player.inventory.setItem(1, Pests.genPestTool(Material.WOODEN_AXE))
		player.inventory.setItem(2, Pests.genPestTool(Material.WOODEN_SHOVEL))
		player.inventory.setItem(3, Pests.genPestTool(Material.WOODEN_HOE))
		player.inventory.setItem(4, Pests.genPestTool(Material.WOODEN_SWORD))
	}

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		/* only do this on pests mode */
		if (!Quirk.PESTS.enabled)
			return

		if (event.target == null)
			return

		if (event.target !is Player)
			return

		var player = event.target as Player

		/* monsters will not target the pests */
		if (Pests.isPest(player))
			event.isCancelled = true
	}

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		/* only do this on pests mode */
		if (!Quirk.PESTS.enabled)
			return

		var player = event.whoClicked;

		if (!Pests.isPest(player as Player))
			return

		var item = event.recipe.result.type

		/* prevent crafting of banned items */
		if (binarySearch(item, Pests.banList))
			event.isCancelled = true
	}

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
	fun onEntityDeath(event: EntityDeathEvent) {
		if (!Quirk.ABUNDANCE.enabled)
			return

		event.drops.forEach { drop ->
			drop.amount = drop.amount * 3
		}
	}

	@EventHandler
	fun onEntityDamageEvent(e : EntityDamageByEntityEvent) {
		if (Quirk.HALF_ZATOICHI.enabled) {
			return
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
			if (Quirk.ABUNDANCE.enabled) enchantThing(fakeTool, LOOT_BONUS_BLOCKS, 5);

			fakeTool;
		}

		/* these replace regular block breaking behavior */
		event.isCancelled = Quirk.UNSHELTERED.enabled || Quirk.ABUNDANCE.enabled;

		if (Quirk.UNSHELTERED.enabled) {
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
		} else if (Quirk.ABUNDANCE.enabled) {
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
		if (Quirk.UNSHELTERED.enabled) {
			var block = event.block;

			if (!binarySearch(block.type, acceptedBlocks)) {
				event.isCancelled = true;
			}
		}
	}
}
