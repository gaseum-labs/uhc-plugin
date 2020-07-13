package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiOpener
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.waiting.WaitingDefault
import com.codeland.uhc.quirk.*
import com.destroystokyo.paper.Title
import com.destroystokyo.paper.utils.PaperPluginLogger
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.material.SpawnEgg
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin
import java.util.logging.Level


class EventListener : Listener {
	@EventHandler
	fun onPlayerHurt(event: EntityDamageEvent) {

		if (Quirk.WET_SPONGE.enabled) {
			if (event.entity is Player) {
				(event.entity as Player).inventory.addItem(ItemStack(Material.WET_SPONGE))
			}
		}

		if (!GameRunner.uhc.isPhase(PhaseType.WAITING) && !GameRunner.uhc.isPhase(PhaseType.POSTGAME))
			return

		if (event.entityType == EntityType.PLAYER)
			event.isCancelled = true
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		Phase.dimensionOne(event.player)

		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			WaitingDefault.onPlayerJoin(event.player)
		} else {
			if (GameRunner.playersTeam(event.player.name) == null)
				event.player.gameMode = GameMode.SPECTATOR

			return
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		/*if (Quirk.MODIFIED_DROPS.enabled) {
			if (ModifiedDrops.isSpawnEgg(event.item?.type)) {
				if (event.action == Action.RIGHT_CLICK_BLOCK) {
					val location = event.clickedBlock!!.location.add(event.blockFace.direction).add(0.5, 0.5, 0.5)
					val entity = event.player.world.spawnEntity(location, ModifiedDrops.getEntityType(event.item?.type)!!, CreatureSpawnEvent.SpawnReason.EGG)
					val team = GameRunner.playersTeam(event.player.name)
					if (team != null) {
						entity.customName = "${team.color}${team.displayName}${ChatColor.RESET} ${entity.name}"
					}
					if (event.item != null) {
						event.item!!.amount = event.item!!.amount - 1
					}
					event.isCancelled = true
				}
			}
		}*/

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
	fun onPlayerDeath(event: PlayerDeathEvent) {
		var wasPest = Pests.isPest(event.entity)

		if (Quirk.PESTS.enabled) {
			if (event.entity.gameMode != GameMode.SPECTATOR)
				Pests.makePest(event.entity)

		} else {
			event.entity.gameMode = GameMode.SPECTATOR
		}

		if (!wasPest && !GameRunner.uhc.isPhase(PhaseType.WAITING))
			GameRunner.playerDeath(event.entity)
	}

	@EventHandler
	fun onMessage(e: AsyncPlayerChatEvent) {
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
	fun onPlayerTeleport(e: PlayerTeleportEvent) {
		if (!GameRunner.netherIsAllowed()) {
			if (e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && e.player.gameMode == GameMode.SURVIVAL) {
				e.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
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

		var y = GameRunner.topBlockY(player.world, x, z)
		event.respawnLocation.set(x + 0.5, y + 1.0, z + 0.5)

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

		if (Quirk.WET_SPONGE.enabled) {
			if (event.target is Player && Math.random() < 0.20) {
				(event.target as Player).inventory.addItem(ItemStack(Material.WET_SPONGE))
			}
		}

		if (Quirk.MODIFIED_DROPS.enabled) {
			if (event.target != null) {
				val team = GameRunner.playersTeam(event.target!!.name)
				if (team != null) {
					if (event.entity.customName != null) {
						if (event.entity.customName!!.startsWith(team.color.toString())) {
							event.isCancelled = true
						}
					}
				}
			}
		}

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

		if (Quirk.WET_SPONGE.enabled) {
			if (Math.random() < 0.1) {
				event.whoClicked as Player
				event.whoClicked.inventory.addItem(ItemStack(Material.WET_SPONGE))
			}
		}

		/* only do this on pests mode */
		if (!Quirk.PESTS.enabled)
			return

		var player = event.whoClicked;

		if (!Pests.isPest(player as Player))
			return

		var item = event.recipe.result.type

		/* prevent crafting of banned items */
		if (GameRunner.binarySearch(item, Pests.banList))
			event.isCancelled = true
	}

	@EventHandler
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		val stack = event.itemDrop.itemStack

		if (Quirk.WET_SPONGE.enabled) {
			event.player.inventory.addItem(ItemStack(Material.WET_SPONGE, 1))
		}

		event.isCancelled = when {
			Zatoichi.isHalfZatoichi(stack) -> true
			GuiOpener.isGuiOpener(stack) -> true
			else -> false
		}
	}

	@EventHandler
	fun onEntityDeath(event: EntityDeathEvent) {
		if (Quirk.MODIFIED_DROPS.enabled) {
			ModifiedDrops.onDrop(event.entityType, event.drops)
		}

		val spawnEgg = Summoner.getSpawnEgg(event.entityType, Quirk.AGGRO_SUMMONER.enabled, Quirk.PASSIVE_SUMMONER.enabled)

		if (spawnEgg != null)
			event.drops.add(ItemStack(spawnEgg))

		if (Quirk.ABUNDANCE.enabled) {
			if (event.entity.killer != null && event.entityType != EntityType.PLAYER) {
				event.drops.forEach { drop ->
					drop.amount = drop.amount * 3
				}
			}
		}
	}

	@EventHandler
	fun onEntityDamageEvent(e: EntityDamageByEntityEvent) {
		if (!Quirk.HALF_ZATOICHI.enabled) {
			return
		}
		if (e.damager is Player && e.entity is Player) {
			val defender = e.entity as Player
			val attacker = e.damager as Player

			if (Zatoichi.isHalfZatoichi(attacker.inventory.itemInMainHand)) {
				if (Zatoichi.isHalfZatoichi(defender.inventory.itemInMainHand)) {
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

	fun enchantThing(item: ItemStack, enchant: Enchantment, level: Int) {
		val meta = item.itemMeta
		meta.addEnchant(enchant, level, true)
		item.itemMeta = meta
	}

	@EventHandler
	fun onBreakBlock(event: BlockBreakEvent) {
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

		val breakBlock = {
			var drops = block.getDrops(getTool())

			if (Quirk.ABUNDANCE.enabled)
				Abundance.extraDrops(block, drops)

			for (drop in drops)
				player.world.dropItem(block.location.add(0.5, 0.5, 0.5), ItemStack(drop.type, drop.amount))

			if (Quirk.UNSHELTERED.enabled) {
				/* make sure we can't break this block again */
				block.state.setMetadata("broken", FixedMetadataValue(GameRunner.plugin as Plugin, true))
			} else {
				block.type = Material.AIR
			}
		}

		/* these replace regular block breaking behavior */
		event.isCancelled = Quirk.UNSHELTERED.enabled || Quirk.ABUNDANCE.enabled;

		if (Quirk.UNSHELTERED.enabled) {
			/* regular block breaking behavior for acceptable blocks */
			if (GameRunner.binarySearch(block.type, acceptedBlocks)) {
				event.isCancelled = false
				return
			}

			var broken = block.state.getMetadata("broken")

			/* if we have not applied broken label or broken is explicitly set to false */
			/* proceed to mine then set broken to true */
			if (broken.size == 0 || !broken[0].asBoolean()) {
				breakBlock()
			} else {
				player.sendActionBar("${ChatColor.GOLD}${ChatColor.BOLD}Block already broken!")
			}
		} else if (Quirk.ABUNDANCE.enabled) {
			breakBlock()
		}

		if (Quirk.WET_SPONGE.enabled) {
			if (Math.random() < 0.01) {
				player.inventory.addItem(ItemStack(Material.WET_SPONGE))
			}
		}
	}

	val acceptedBlocks = arrayOf<Material>(
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
		Material.BARREL,
		Material.WET_SPONGE,
		Material.TNT
	)

	init {
		acceptedBlocks.sort()
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		if (Quirk.WET_SPONGE.enabled) {
			if (event.block.type == Material.WET_SPONGE) {
				event.isCancelled = true
				event.player.inventory.addItem(ItemStack(Material.WET_SPONGE))
			}
		}

		if (Quirk.CREATIVE.enabled) {
			var material = event.itemInHand.type

			/* replace these blocks */
			if (GameRunner.binarySearch(material, Creative.blocks)) {

				var oldItemStack = event.itemInHand.clone()

				val inHand: ItemStack = if (event.hand === EquipmentSlot.HAND)
					event.player.inventory.itemInMainHand.clone()
				else
					event.player.inventory.itemInOffHand.clone()

				Bukkit.getScheduler().runTaskLater(GameRunner.plugin, {
					if (event.hand === EquipmentSlot.HAND)
						event.player.inventory.setItemInMainHand(inHand)
					else
						event.player.inventory.setItemInOffHand(inHand)
				} as () -> Unit, 0)
			}

		} else if (Quirk.UNSHELTERED.enabled) {
			var block = event.block

			if (!GameRunner.binarySearch(block.type, acceptedBlocks)) {
				event.isCancelled = true
			}
		}
	}
}