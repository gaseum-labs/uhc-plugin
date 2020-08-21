package com.codeland.uhc.event

import com.codeland.uhc.command.TeamData
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.util.Util
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiOpener
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.waiting.WaitingDefault
import com.codeland.uhc.quirk.*
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class EventListener : Listener {
	@EventHandler
	fun onPlayerHurt(event: EntityDamageEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			val player = event.entity

			if (player is Player)
				WetSponge.addSponge(player)
		}

		if (GameRunner.uhc.isEnabled(QuirkType.LOW_GRAVITY) && event.cause == EntityDamageEvent.DamageCause.FALL) {
			event.isCancelled = true
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
		if (GameRunner.uhc.isEnabled(QuirkType.COMMANDER) || GameRunner.uhc.isEnabled(QuirkType.AGGRO_SUMMONER) || GameRunner.uhc.isEnabled(QuirkType.PASSIVE_SUMMONER))
			if (Commander.onSummon(event)) event.isCancelled = true

		/* only can open uhc settings while in waiting */
		if (!GameRunner.uhc.isPhase(PhaseType.WAITING))
			return

		val stack = event.item
				?: return

		if (!GuiOpener.isGuiOpener(stack))
			return

		Gui.open(event.player)
	}

	@EventHandler
	fun onHunger(event: FoodLevelChangeEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity

		var wasPest = Pests.isPest(player)

		if (GameRunner.uhc.isEnabled(QuirkType.PESTS)) {
			if (event.entity.gameMode != GameMode.SPECTATOR) {
				val team = GameRunner.playersTeam(player.name)
				if (team != null) TeamData.removeFromTeam(team, player.name)

				Pests.makePest(player)
			}
		} else {
			player.gameMode = GameMode.SPECTATOR
		}

		if (GameRunner.uhc.isEnabled(QuirkType.HALF_ZATOICHI)) {
			val killer = player.killer
			if (killer != null) HalfZatoichi.onKill(killer)
		}

		if (!wasPest && !GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			GameRunner.playerDeath(player)
		}
	}

	@EventHandler
	fun onMessage(event: AsyncPlayerChatEvent) {
		/* only modify chat behavior with players on teams */
		val team = GameRunner.playersTeam(event.player.name) ?: return

		/* only modify chat when game is running */
		if (GameRunner.uhc.isPhase(PhaseType.WAITING) || GameRunner.uhc.isPhase(PhaseType.POSTGAME)) return

		if (event.message.startsWith("!")) {
			event.message = event.message.substring(1)

		} else {
			event.isCancelled = true

			val component = "${team.color}<${event.player.displayName}> ${ChatColor.RESET}${event.message}"

			team.entries.forEach { entry ->
				Bukkit.getPlayer(entry)?.sendMessage(component)
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
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			if (event.entityType.isAlive) {
				event.isCancelled = true
				return
			}
		}
	}

	@EventHandler
	fun onPlayerRespawn(event: PlayerRespawnEvent) {
		/* only do this on pests mode */
		if (!GameRunner.uhc.isEnabled(QuirkType.PESTS))
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

		var y = Util.topBlockY(player.world, x, z)
		event.respawnLocation.set(x + 0.5, y + 1.0, z + 0.5)

		Pests.givePestSetup(player)
	}

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			val player = event.target

			if (player is Player && Math.random() < 0.20)
				WetSponge.addSponge(player)
		}

		if (GameRunner.uhc.isEnabled(QuirkType.COMMANDER)) {
			val target = event.target ?: return

			if (target !is Player)
				return

			val team = GameRunner.playersTeam(target.name) ?: return

			if (Commander.isCommandedBy(event.entity, team.color))
				event.isCancelled = true
		}

		/* preventing pest targeting */

		if (!GameRunner.uhc.isEnabled(QuirkType.PESTS))
			return

		if (event.target == null)
			return

		if (event.target !is Player)
			return

		var player = event.target as Player

		if (Pests.isPest(player))
			event.isCancelled = true
	}

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			if (Math.random() < 0.1) {
				var player = event.whoClicked as Player
				WetSponge.addSponge(player)
			}
		}

		/* prevent pest crafting */

		if (!GameRunner.uhc.isEnabled(QuirkType.PESTS))
			return

		var player = event.whoClicked

		if (!Pests.isPest(player as Player))
			return

		var item = event.recipe.result.type

		if (Util.binarySearch(item, Pests.banList))
			event.isCancelled = true
	}

	@EventHandler
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		val stack = event.itemDrop.itemStack

		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			WetSponge.addSponge(event.player)
		}

		event.isCancelled = when {
			HalfZatoichi.isHalfZatoichi(stack) -> true
			GuiOpener.isGuiOpener(stack) -> true
			else -> false
		}
	}

	@EventHandler
	fun onHealthRegen(event: EntityRegainHealthEvent) {
		/* no regeneration in UHC */
		var player = event.entity

		if (player is Player) {

			val test = emptyArray<ItemStack?>().copyOf()

			player.inventory.contents.copyOf().contentEquals(test)


		}

		/* make sure it only applies to players */
		/* make sure it only applies to regeneration due to hunger */
		if (player is Player && event.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED) {
			if (!(GameRunner.uhc.isPhase(PhaseType.WAITING) || GameRunner.uhc.isPhase(PhaseType.GRACE))) {
				/* pests can regenerate */
				if (GameRunner.uhc.isEnabled(QuirkType.PESTS)) {
					event.isCancelled = !Pests.isPest(player)
				} else {
					event.isCancelled = true
				}
			}
		}
	}

	@EventHandler
	fun onEntityDeath(event: EntityDeathEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.MODIFIED_DROPS)) {
			ModifiedDrops.onDrop(event.entityType, event.drops)
		}

		if (!Commander.isCommanded(event.entity)) {
			val spawnEgg = Summoner.getSpawnEgg(event.entityType, GameRunner.uhc.isEnabled(QuirkType.AGGRO_SUMMONER), GameRunner.uhc.isEnabled(QuirkType.PASSIVE_SUMMONER))

			if (spawnEgg != null)
				event.drops.add(ItemStack(spawnEgg))
		}

		if (GameRunner.uhc.isEnabled(QuirkType.ABUNDANCE)) {
			if (event.entity.killer != null && event.entityType != EntityType.PLAYER) {
				event.drops.forEach { drop ->
					drop.amount = drop.amount * 3
				}
			}
		}
	}

	@EventHandler
	fun onEntityDamageEvent(event: EntityDamageByEntityEvent) {
		var attacker = event.damager
		var defender = event.entity

		if (attacker is Player && defender is Player) {
			/* protected no pvp phases */
			if (
				GameRunner.uhc.isPhase(PhaseType.WAITING) ||
				GameRunner.uhc.isPhase(PhaseType.GRACE) ||
				GameRunner.uhc.isPhase(PhaseType.POSTGAME)
			) {
				event.isCancelled = true
				return
			}

			/* pests cannot attack each other */
			if (GameRunner.uhc.isEnabled(QuirkType.PESTS) && Pests.isPest(attacker) && Pests.isPest(defender))
				event.isCancelled = true

			if (GameRunner.uhc.isEnabled(QuirkType.HALF_ZATOICHI)) {
				if (HalfZatoichi.isHalfZatoichi(attacker.inventory.itemInMainHand) && HalfZatoichi.isHalfZatoichi(defender.inventory.itemInMainHand)) {
					event.damage = 1000000000.0
				}
			}
		}
	}

	@EventHandler
	fun onBlockDrop(event: BlockDropItemEvent) {
		var blockState = event.blockState
		var block = event.block
		var player = event.player
		var baseItem = event.player.inventory.itemInMainHand
		var drops = event.items
		var blockMiddle = blockState.location.add(0.5, 0.5, 0.5)

		if (GameRunner.uhc.isEnabled(QuirkType.APPLE_FIX) && AppleFix.isLeaves(blockState.type) && !(drops.size > 0 && AppleFix.isLeaves(drops[0].itemStack.type))) {
			drops.clear()

			AppleFix.onbreakLeaves(blockState.type, player).forEach { drop ->
				player.world.dropItem(blockMiddle, drop)
			}

		} else if (GameRunner.uhc.isEnabled(QuirkType.ABUNDANCE)) {
			Abundance.replaceDrops(player, block, blockState, drops)
		}
	}

	@EventHandler
	fun onBreakBlock(event: BlockBreakEvent) {
		var block = event.block
		var player = event.player
		var baseItem = event.player.inventory.itemInMainHand;

		if (GameRunner.uhc.isEnabled(QuirkType.UNSHELTERED) && !Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
			var broken = block.state.getMetadata("broken")

			var oldBlockType = block.type
			var oldData = block.blockData

			/* block has not been set as broken */
			if (Unsheltered.isBroken(block)) {
				player.sendActionBar("${ChatColor.GOLD}${ChatColor.BOLD}Block already broken!")
				event.isCancelled = true

			} else {
				Bukkit.getScheduler().runTaskLater(GameRunner.plugin, {
					block.type = oldBlockType
					block.blockData = oldData
					Unsheltered.setBroken(block, true)

				} as () -> Unit, 0)
			}
		}

		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			if (Math.random() < 0.01)
				WetSponge.addSponge(player)
		}
	}

	@EventHandler
	fun onDecay(event: LeavesDecayEvent) {
		if (!GameRunner.uhc.isEnabled(QuirkType.APPLE_FIX))
			return

		/* drop apple for the nearest player */
		Bukkit.getOnlinePlayers().any { player ->
			if (player.location.distance(event.block.location) < 16) {
				AppleFix.onbreakLeaves(event.block.type, player).forEach { drop ->
					player.world.dropItem(event.block.location.add(0.5, 0.5, 0.5), drop)
				}

				true
			}

			false
		}

		/* prevent drops */
		event.isCancelled = true
		event.block.type = Material.AIR
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			if (event.block.type == Material.WET_SPONGE) {
				event.isCancelled = true
				WetSponge.addSponge(event.player)
			}
		}

		if (GameRunner.uhc.isEnabled(QuirkType.CREATIVE)) {
			var material = event.itemInHand.type

			/* replace these blocks */
			if (Util.binarySearch(material, Creative.blocks)) {

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

		} else if (GameRunner.uhc.isEnabled(QuirkType.UNSHELTERED)) {
			var block = event.block

			if (!Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
				event.isCancelled = true
			}
		}
	}
}