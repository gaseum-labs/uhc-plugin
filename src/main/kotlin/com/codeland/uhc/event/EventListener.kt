package com.codeland.uhc.event

import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.dropFix.DropFixType
import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.phase.DimensionBar
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.phase.phases.endgame.EndgameNaturalTerrain
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.phase.phases.waiting.LobbyPvp
import com.codeland.uhc.phase.phases.waiting.WaitingDefault
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.NetherFix
import net.md_5.bungee.api.ChatColor
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class EventListener : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val player = event.player

		NameManager.updateName(event.player)
		DimensionBar.setPlayerBarDimension(event.player)

		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			(GameRunner.uhc.currentPhase as WaitingDefault?)?.onPlayerJoin(event.player)

		} else if (GameRunner.uhc.isGameGoing() && (!GameRunner.uhc.isParticipating(player.uniqueId) || !GameRunner.uhc.isAlive(player.uniqueId))) {
			event.player.gameMode = GameMode.SPECTATOR
		}
	}

	@EventHandler
	fun onLogOut(event: PlayerQuitEvent) {
		val player = event.player

		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			val pvpData = LobbyPvp.getPvpData(player)

			if (pvpData.inPvp) LobbyPvp.disablePvp(player)

		} else if (GameRunner.uhc.isGameGoing()) {
			val playerData = GameRunner.uhc.getPlayerData(player.uniqueId)

			if (playerData.participating && playerData.alive) {
				playerData.offlineZombie = playerData.createZombie(player)
			}
		}
	}

	@EventHandler
	fun onPlayerHurt(event: EntityDamageEvent) {
		if (GameRunner.uhc.isGameGoing()) {
			if (GameRunner.uhc.isEnabled(QuirkType.LOW_GRAVITY) && event.cause == EntityDamageEvent.DamageCause.FALL) {
				event.isCancelled = true
			} else if (event.entity is Player && GameRunner.uhc.isEnabled(QuirkType.DEATHSWAP) && Deathswap.swapTime < Deathswap.IMMUNITY) {
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
		if (summoner.enabled && summoner.onSummon(event)) {
			event.isCancelled = true

		} else if (GameRunner.uhc.isPhase(PhaseType.WAITING) && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
			CommandItemType.commandItemList.any { commandItem ->
				if (commandItem.isItem(stack)) {
					commandItem.onUse(GameRunner.uhc, event.player)
					true

				} else false
			}
		}
	}

	@EventHandler
	fun onDropItem(event: ItemSpawnEvent) {
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			event.entity.ticksLived = 5000
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity

		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			LobbyPvp.allInPvp { pvpPlayer, pvpData ->
				if (pvpData.inPvp) pvpPlayer.sendMessage(event.deathMessage ?: "${player.name} fucking died")
			}

		} else if (GameRunner.uhc.isEnabled(QuirkType.BETRAYAL)) {
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
				TeamData.removeFromTeam(player.uniqueId, true)
		}

		/* remove chc specific items from drops */

		if (GameRunner.uhc.isEnabled(QuirkType.HOTBAR)) {
			event.drops.removeAll { itemStack ->
				itemStack.type == Material.BLACK_STAINED_GLASS_PANE && itemStack.itemMeta.displayName == "Unusable Slot"
			}
		}

		if (GameRunner.uhc.isEnabled(QuirkType.PLAYER_COMPASS)) PlayerCompass.filterDrops(event.drops)
	}

	@EventHandler
	fun onWeather(event: WeatherChangeEvent) {
		event.isCancelled = GameRunner.uhc.isPhase(PhaseType.WAITING) && event.toWeatherState()
	}

	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
		if (GameRunner.uhc.isGameGoing()) {
			event.isCancelled = (
				event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL
			) && event.entity is Monster

		} else {
			event.isCancelled = (
				event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL
			) && event.entityType.isAlive
		}
	}

	@EventHandler
	fun onPlayerRespawn(event: PlayerRespawnEvent) {
		val player = event.player
		val uuid = player.uniqueId

		/* waiting pvp respawning */
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			if (LobbyPvp.getPvpData(player).inPvp) LobbyPvp.disablePvp(player)

		/* respawning when the game is going */
		} else {
			/* conditions to respawn in game */
			if (GameRunner.uhc.isAlive(uuid) || (GameRunner.uhc.isEnabled(QuirkType.PESTS) && Pests.isPest(player))) {
				val world = Util.worldFromEnvironment(GameRunner.uhc.defaultEnvironment)
				val location = GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
				if (location != null) event.respawnLocation = location

				/* custom quirk behavior when players start */
				GameRunner.uhc.quirks.forEach { quirk ->
					if (quirk.enabled) quirk.onStart(event.player.uniqueId)
				}

			/* otherwise this is a spectator */
			} else {
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

		event.isCancelled =
			(GameRunner.uhc.isPhase(PhaseType.WAITING) && CommandItemType.commandItemList.any { commandItem ->
				commandItem.isItem(stack)
			}) ||
			(GameRunner.uhc.isEnabled(QuirkType.PLAYER_COMPASS) && PlayerCompass.isCompass(stack))
	}

	fun shouldHealthCancelled(player: Player): Boolean {
		return (GameRunner.uhc.isPhase(PhaseType.WAITING) && LobbyPvp.getPvpData(player).inPvp) ||
			(!GameRunner.uhc.naturalRegeneration && (!GameRunner.uhc.isPhase(PhaseType.GRACE) && (!GameRunner.uhc.isEnabled(QuirkType.PESTS) || !Pests.isPest(player))))
	}

	@EventHandler
	fun onHealthRegen(event: EntityRegainHealthEvent) {
		/* no regeneration in UHC */
		var player = event.entity

		/* make sure it only applies to players */
		/* make sure it only applies to regeneration due to hunger */
		if (player is Player && event.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED) {
			if (shouldHealthCancelled(player)) {
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onFoodLevelChange(event: FoodLevelChangeEvent) {
		val player = event.entity as Player

		if (GameRunner.uhc.isPhase(PhaseType.WAITING) && !LobbyPvp.getPvpData(player).inPvp) {
			event.foodLevel = 20
			event.isCancelled = true

		} else if (shouldHealthCancelled(player)) {
			val over = (event.foodLevel - 17).coerceAtLeast(0)

			if (event.foodLevel > 17) event.foodLevel = 17
			player.saturation += over
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

		if (GameRunner.uhc.isEnabled(QuirkType.HALLOWEEN)) {
			Halloween.addDrops(event.entity, event.drops)
			Halloween.onEntityDeath(event.entity)
		}

		if (GameRunner.uhc.isEnabled(QuirkType.ABUNDANCE)) {
			if (event.entity.killer != null && event.entityType != EntityType.PLAYER) {
				event.drops.forEach { drop ->
					drop.amount = drop.amount * 3
				}
			}
		}
	}

	/**
	 * brewfix
	 *
	 * prevents strength II from being brewed,
	 * also transmutes all regen potions into instant health potions
	 */
	@EventHandler
	fun onBrew(event: BrewEvent) {
		fun containsStrength(contents: Array<ItemStack>) = contents.any { itemStack ->
			if (itemStack == null || !(itemStack.type == Material.POTION || itemStack.type == Material.SPLASH_POTION || itemStack.type == Material.LINGERING_POTION)) return@any false
			val meta = itemStack.itemMeta as PotionMeta

			meta.basePotionData.type == PotionType.STRENGTH
		}

		if (event.contents.ingredient?.type == Material.GLOWSTONE_DUST && containsStrength(event.contents.contents)) {
			/* eject glowstone from the brewing stand */
			val ingredient = event.contents.ingredient?.clone()
			if (ingredient != null) event.block.world.dropItem(event.block.location.toCenterLocation(), ingredient)
			event.contents.ingredient = null

			/* prevent potions from being brewed */
			event.isCancelled = true

		} else if (event.contents.ingredient?.type == Material.GHAST_TEAR) {
			event.isCancelled = true
			event.contents.ingredient = null

			event.contents.contents.forEach { itemStack ->
				if (itemStack != null && (itemStack.type == Material.POTION || itemStack.type == Material.SPLASH_POTION || itemStack.type == Material.LINGERING_POTION)) {
					val meta = itemStack.itemMeta as PotionMeta

					if (meta.basePotionData.type == PotionType.AWKWARD) {
						meta.basePotionData = PotionData(PotionType.INSTANT_HEAL)
						itemStack.itemMeta = meta
					}
				}
			}
		}
	}

	@EventHandler
	fun onEntityDamageEvent(event: EntityDamageByEntityEvent) {
		var attacker = event.damager
		var defender = event.entity

		if (attacker is Projectile && defender is Player) {
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

		if (type == Material.NETHER_WART && GameRunner.uhc.isGameGoing() && block.world.environment == World.Environment.NETHER) {
			drops.clear()
			block.world.dropItem(blockMiddle, ItemStack(Material.NETHER_WART))

		} else if (event.player.gameMode != GameMode.CREATIVE &&
			BlockFixType.values().any { blockFixType ->
				blockFixType.blockFix.onBreakBlock(GameRunner.uhc, type, drops, player) { drop ->
					if (drop != null) block.world.dropItem(blockMiddle, drop)
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
		var baseItem = event.player.inventory.itemInMainHand

		/* prevent breaking the border in creative lobby */
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			val x = GameRunner.uhc.lobbyX
			val z = GameRunner.uhc.lobbyZ
			val radius = GameRunner.uhc.lobbyRadius + 1

			if (((block.x == x + radius || block.x == x - radius) &&
					(block.z > z - radius) && (block.z < z + radius)) ||
				((block.z == z + radius || block.z == z - radius) &&
					(block.x > x - radius) && (block.x < x + radius)) ||
				((block.y == 255 || block.y == 0) && block.x <= x + radius && block.z <= z + radius && block.x >= x - radius && block.z >= z - radius)
			) event.isCancelled = true

		} else if (GameRunner.uhc.isEnabled(QuirkType.UNSHELTERED) && !Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
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
	}

	@EventHandler
	fun onDecay(event: LeavesDecayEvent) {
		/* prevent default drops */
		val leavesType = event.block.type

		event.isCancelled = true
		event.block.type = Material.AIR

		val leavesLocation = event.block.location.toCenterLocation()

		var nearestPlayer = null as Player?
		var nearestDistance = Double.MAX_VALUE

		/* find the nearest player to the decaying leaves */
		Bukkit.getOnlinePlayers().forEach { player ->
			val playerLocation = player.location

			if (leavesLocation.world == playerLocation.world) {
				val distance = playerLocation.distance(leavesLocation)
				if (distance < nearestDistance) {
					nearestDistance = distance
					nearestPlayer = player
				}
			}
		}

		val dropPlayer = nearestPlayer

		/* apply applefix to this leaves block for the nearest player */
		if (dropPlayer != null) {
			BlockFixType.LEAVES.blockFix.onBreakBlock(leavesType, dropPlayer) { drop ->
				if (drop != null) leavesLocation.world.dropItem(event.block.location.toCenterLocation(), drop)
			}
		}
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		val phase = GameRunner.uhc.currentPhase

		if (phase is WaitingDefault && LobbyPvp.getPvpData(event.player).inPvp && event.blockPlaced.y > GameRunner.uhc.lobbyPvpHeight) {
			event.player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Height limit for building is ${GameRunner.uhc.lobbyPvpHeight}")
			event.isCancelled = true

		} else if (phase is EndgameNaturalTerrain && event.blockPlaced.y > phase.topBoundary) {
			event.player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Height limit for building is ${phase.topBoundary}")
			event.isCancelled = true

		} else {
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
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.HOTBAR))
			if (event.clickedInventory?.type == InventoryType.PLAYER && event.slot in 9..35) {
				event.isCancelled = true
			}
	}

	@EventHandler
	fun onPickUpItem(event: EntityPickupItemEvent) {
		if (
			GameRunner.uhc.isEnabled(QuirkType.HALLOWEEN) &&
			event.entityType == EntityType.PLAYER &&
			event.item.itemStack.type == Material.DIAMOND &&
			GameRunner.uhc.isGameGoing() &&
			event.entity.name != "balduvian"
		) {
			val halloween = GameRunner.uhc.getQuirk(QuirkType.HALLOWEEN) as Halloween

			if (!halloween.hasGottenDiamonds) {
				Halloween.jumpScare(event.entity as Player)
				halloween.hasGottenDiamonds = true
			}
		}
	}
}
