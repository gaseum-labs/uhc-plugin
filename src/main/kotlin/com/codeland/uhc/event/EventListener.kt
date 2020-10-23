package com.codeland.uhc.event

import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.*
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.dropFix.DropFixType
import com.codeland.uhc.gui.item.AntiSoftlock
import com.codeland.uhc.util.Util
import com.codeland.uhc.gui.item.GuiOpener
import com.codeland.uhc.gui.item.ParkourCheckpoint
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.phase.phases.waiting.LobbyPvp
import com.codeland.uhc.phase.phases.waiting.WaitingDefault
import com.codeland.uhc.quirk.*
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.quirk.quirks.Betrayal.BetrayalData
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.world.NetherFix
import net.md_5.bungee.api.ChatColor
import org.bukkit.*
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EventListener : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val player = event.player

		NameManager.updateName(event.player)
		Phase.setPlayerBarDimension(event.player)

		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			(GameRunner.uhc.currentPhase as WaitingDefault?)?.onPlayerJoin(event.player)

		} else if (!GameRunner.uhc.isParticipating(player.uniqueId) || !GameRunner.uhc.isAlive(player.uniqueId)) {
			event.player.gameMode = GameMode.SPECTATOR
		}
	}

	@EventHandler
	fun onLogOut(event: PlayerQuitEvent) {
		Util.log("${event.player.name} quitting")

		if (GameRunner.uhc.isGameGoing()) {
			val player = event.player

			val playerData = GameRunner.uhc.getPlayerData(player.uniqueId)

			if (playerData.participating && playerData.alive) {
				playerData.offlineZombie = playerData.createZombie(player)
				Util.log("created zombie named: ${playerData.offlineZombie?.customName}")
			}
		}
	}

	@EventHandler
	fun onPlayerHurt(event: EntityDamageEvent) {
		if (GameRunner.uhc.isGameGoing()) {
			if (GameRunner.uhc.isEnabled(QuirkType.LOW_GRAVITY) && event.cause == EntityDamageEvent.DamageCause.FALL) {
				event.isCancelled = true

			} else if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
				val player = event.entity
				if (player is Player) WetSponge.addSponge(player)
				
			}
			if (event.entity is Player && GameRunner.uhc.isEnabled(QuirkType.DEATHSWAP) && Deathswap.swapTime < Deathswap.IMMUNITY) {
				event.isCancelled = true
			}
		} else {
			if (!GameRunner.uhc.isGameGoing() && event.entityType == EntityType.PLAYER) {
				event.isCancelled = true
				val player = event.entity as Player
				if (LobbyPvp.getPvpData(player).inPvp) {
					event.isCancelled = false
				}
			}
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		val stack = event.item ?: return

		val summoner = GameRunner.uhc.getQuirk(QuirkType.SUMMONER) as Summoner
		if (summoner.enabled && summoner.onSummon(event)) event.isCancelled = true

		if (GuiOpener.isItem(stack)) {
			GameRunner.uhc.gui.inventory.open(event.player)
		} else if (AntiSoftlock.isItem(stack)) {
			val world = Bukkit.getWorlds()[0]
			val center = (GameRunner.uhc.currentPhase as WaitingDefault?)?.center ?: 10000

			event.player.teleport(Location(world, center + 0.5, Util.topBlockY(world, center, center) + 1.0, center + 0.5))
		} else if (ParkourCheckpoint.isItem(stack)) {
			val location = ParkourCheckpoint.getPlayerCheckpoint(event.player)?.toBlockLocation()
				?: return Commands.errorMessage(event.player, "Reach a gold block to get a checkpoint!")

			val block = Bukkit.getWorlds()[0].getBlockAt(location.clone().subtract(0.0, 1.0, 0.0).toBlockLocation())
			if (block.type != ParkourCheckpoint.CHECKPOINT)
				return Commands.errorMessage(event.player, "Checkpoint has been removed!")

			event.player.teleport(location.add(0.5, 0.0, 0.5))
		}
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

		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			if (LobbyPvp.getPvpData(player).inPvp) event.drops.clear()

			return
		}

		if (GameRunner.uhc.isEnabled(QuirkType.HALF_ZATOICHI)) {
			val killer = player.killer
			if (killer != null) HalfZatoichi.onKill(killer)
		}

		if (GameRunner.uhc.isEnabled(QuirkType.BETRAYAL)){
			val killer = player.killer

			/* don't drop if you were killed and team swapped */
			if(killer != null) {
				event.keepInventory = true
				event.drops.clear()

				Betrayal.onPlayerDeath(player.uniqueId, killer.uniqueId)
			}
		/* regular deaths */
		} else if (!(GameRunner.uhc.isVariant(PhaseVariant.GRACE_FORGIVING))) {
			val wasPest = Pests.isPest(player)

			if (GameRunner.uhc.isEnabled(QuirkType.PESTS)) {
				if (!wasPest && GameRunner.uhc.isAlive(player.uniqueId))
					Pests.makePest(player)
			} else {
				player.gameMode = GameMode.SPECTATOR
			}

			if (GameRunner.uhc.isAlive(player.uniqueId) && !wasPest)
				GameRunner.playerDeath(player.uniqueId, player.killer)

			if (Pests.isPest(player))
				TeamData.removeFromTeam(player.uniqueId, true) {}
		}

		/* prevent glass from dropping in hotbar chc */
		if (GameRunner.uhc.isEnabled(QuirkType.HOTBAR)) {
			event.drops.removeAll { itemStack ->
				itemStack.type == Material.BLACK_STAINED_GLASS_PANE && itemStack.itemMeta.displayName == "Unusable Slot"
			}
		}
	}

	@EventHandler
	fun onWeather(event: WeatherChangeEvent) {
		event.isCancelled = GameRunner.uhc.isPhase(PhaseType.WAITING) && event.toWeatherState()
	}

	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			event.isCancelled = (
				event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL ||
				event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.BEEHIVE
			) && event.entityType.isAlive

		} else {
			val world = event.location.world

			if (world.environment == World.Environment.NETHER && GameRunner.netherWorldFix)
				event.isCancelled = NetherFix.replaceSpawn(event.entity)
		}
	}

	private fun spreadRespawn(event: PlayerRespawnEvent, world: World) {
		val location = GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
		if (location != null) event.respawnLocation = location
	}

	@EventHandler
	fun onPlayerRespawn(event: PlayerRespawnEvent) {
		when {
			GameRunner.uhc.isPhase(PhaseType.WAITING) -> {
				/* waiting pvp respawning */
				if (LobbyPvp.getPvpData(event.player).inPvp) {
					LobbyPvp.disablePvp(event.player, LobbyPvp.getPvpData(event.player))
				}
			}
			GameRunner.uhc.isAlive(event.player.uniqueId) -> {
				/* grace respawning */
				spreadRespawn(event, Util.worldFromEnvironment(GameRunner.uhc.defaultEnvironment))
			}
			GameRunner.uhc.isEnabled(QuirkType.PESTS) -> {
				/* pest respawning */
				var player = event.player

				/* player is set to pest on death */
				if (!Pests.isPest(player))
					return

				/* spread player */
				spreadRespawn(event, Util.worldFromEnvironment(GameRunner.uhc.defaultEnvironment))

				Pests.givePestSetup(player)
			}
			else -> {
				/* players respawning as spectator */
				event.respawnLocation = GameRunner.uhc.spectatorSpawnLocation()
			}
		}
	}

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		/* offline zombie targeting */
		val target = event.target ?: return

		if (PlayerData.isZombie(target) && (event.entity.type == EntityType.IRON_GOLEM || event.entity.type == EntityType.SNOWMAN)) {
			event.isCancelled = true
		}

		/* player targeting */
		if (target !is Player) return

		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			if (Math.random() < 0.20) WetSponge.addSponge(target)
		}

		val summoner = GameRunner.uhc.getQuirk(QuirkType.SUMMONER) as Summoner
		if (summoner.enabled && summoner.commander.value) {
			val team = TeamData.playersTeam(target.uniqueId)

			if (team != null && Summoner.isCommandedBy(event.entity, team))
				event.isCancelled = true
		}

		if (GameRunner.uhc.isEnabled(QuirkType.PESTS)) {
			if (Pests.isPest(target))
				event.isCancelled = true
		}
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

		if (GameRunner.uhc.isEnabled(QuirkType.PESTS)) {
			val player = event.whoClicked

			if (!Pests.isPest(player as Player)) return

			event.isCancelled = Util.binarySearch(event.recipe.result.type, Pests.banList)
		}
	}

	@EventHandler
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		val stack = event.itemDrop.itemStack

		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			WetSponge.addSponge(event.player)
		}

		event.isCancelled = when {
			HalfZatoichi.isHalfZatoichi(stack) -> true
			GuiOpener.isItem(stack) -> true
			AntiSoftlock.isItem(stack) -> true
			ParkourCheckpoint.isItem(stack) -> true
			else -> false
		}
	}

	@EventHandler
	fun onBlockBreaking(event: BlockDamageEvent) {
		if (
			GameRunner.uhc.mushroomBlockNerf && (
				event.block.type == Material.RED_MUSHROOM_BLOCK ||
				event.block.type == Material.BROWN_MUSHROOM_BLOCK
			)
		) {
			event.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, 20, 2, true, false, false))
		}
	}

	@EventHandler
	fun onHealthRegen(event: EntityRegainHealthEvent) {
		/* no regeneration in UHC */
		var player = event.entity

		/* make sure it only applies to players */
		/* make sure it only applies to regeneration due to hunger */
		if (player is Player && event.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED) {
			if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
				event.isCancelled = LobbyPvp.getPvpData(player).inPvp
			}
			if (!(GameRunner.uhc.isPhase(PhaseType.GRACE))) {
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
		/* offline zombie killed */
		val (inventory, experience, uuid) = PlayerData.getZombieData(event.entity)
		if (experience != -1) {
			val playerData = GameRunner.uhc.getPlayerData(uuid)
			playerData.offlineZombie = null

			event.drops.clear()

			val killer = event.entity.killer

			if (GameRunner.uhc.isEnabled(QuirkType.BETRAYAL) && killer != null) {
				Betrayal.onPlayerDeath(uuid, killer.uniqueId)

			/* player is allowed to respawn */
			} else {
				inventory.forEach { drop ->
					event.drops.add(drop)
				}

				val droppedExperience = experience.coerceAtMost(100)
				event.droppedExp = droppedExperience

				if (GameRunner.uhc.isVariant(PhaseVariant.GRACE_FORGIVING)) {
					val world = Util.worldFromEnvironment(GameRunner.uhc.defaultEnvironment)

					GameRunner.teleportPlayer(
						uuid,
						GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
							?: Location(world, 0.5, Util.topBlockY(world, 0, 0) + 1.0, 0.5)
					)

					/* player is eliminated */
				} else {
					GameRunner.playerDeath(uuid, event.entity.killer)

					/* reset player for when they rejoin */
					GameRunner.playerAction(uuid) { player ->
						player.health = 20.0
						player.inventory.clear()
						player.fireTicks = -1
						player.totalExperience = 0
						player.activePotionEffects.clear()
						player.teleport(GameRunner.uhc.spectatorSpawnLocation())
					}
				}
			}

			return
		}

		if (GameRunner.uhc.isEnabled(QuirkType.MODIFIED_DROPS)) {
			ModifiedDrops.onDrop(event.entityType, event.drops)
		} else {
			val killer = event.entity.killer

			DropFixType.values().any { dropFix ->
				if (killer == null) dropFix.dropFix.onNaturalDeath(event.entity, event.drops)
				else dropFix.dropFix.onKillEntity(killer, event.entity, event.drops)
			}
		}

		val summoner = GameRunner.uhc.getQuirk(QuirkType.SUMMONER) as Summoner
		if (!Summoner.isCommanded(event.entity) && summoner.enabled) {
			val spawnEgg = summoner.getSpawnEgg(event.entityType)

			if (spawnEgg != null) event.drops.add(ItemStack(spawnEgg))
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

		if (attacker is Arrow && defender is Player) {
			if (
				GameRunner.uhc.isPhase(PhaseType.GRACE) &&
				attacker.shooter is Player
			) {
				event.isCancelled = true
			}

		} else if (attacker is Player && defender is Player) {
			/* protected no pvp phases */
			if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
				event.isCancelled = !LobbyPvp.getPvpData(attacker).inPvp || !LobbyPvp.getPvpData(defender).inPvp
			}

			if (GameRunner.uhc.isPhase(PhaseType.GRACE)) {
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
		} else if (attacker is Player && defender is Zombie) {
			if (PlayerData.isZombie(defender) && GameRunner.uhc.isPhase(PhaseType.GRACE)) {
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onBlockDrop(event: BlockDropItemEvent) {
		var blockState = event.blockState
		var block = event.block
		val type = blockState.type

		var player = event.player
		var drops = event.items
		var blockMiddle = block.location.toCenterLocation()

		if (event.player.gameMode != GameMode.CREATIVE &&
			BlockFixType.values().any { blockFixType ->
				blockFixType.blockFix.onBreakBlock(GameRunner.uhc, type, drops, player) { drop ->
					player.world.dropItem(blockMiddle, drop)
				}
			}
		) else if (GameRunner.uhc.isEnabled(QuirkType.ABUNDANCE)) {
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
				SchedulerUtil.nextTick {
					block.type = oldBlockType
					block.blockData = oldData
					Unsheltered.setBroken(block, true)
				}
			}
		}

		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			if (Math.random() < 0.01)
				WetSponge.addSponge(player)
		}
	}

	@EventHandler
	fun onDecay(event: LeavesDecayEvent) {
		val world = event.block.world

		if (!GameRunner.uhc.appleFix)
			return

		/* drop apple for the nearest player */
		Bukkit.getOnlinePlayers().any { player ->
			if (
				world == player.world &&
				player.location.distance(event.block.location.toCenterLocation()) < 16
			) {
				BlockFixType.LEAVES_FIX.blockFix.onBreakBlock(event.block.type, player) { drop ->
					player.world.dropItem(event.block.location.toCenterLocation(), drop)
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

				val inHand: ItemStack = event.itemInHand.clone()

				SchedulerUtil.nextTick {
					if (event.hand === EquipmentSlot.HAND)
						event.player.inventory.setItemInMainHand(inHand)
					else
						event.player.inventory.setItemInOffHand(inHand)
				}
			}

		} else if (GameRunner.uhc.isEnabled(QuirkType.UNSHELTERED)) {
			var block = event.block

			if (!Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.HOTBAR))
			if (event.clickedInventory?.type == InventoryType.PLAYER && event.slot in 9..35) {
				event.isCancelled = true
			}
	}
}
