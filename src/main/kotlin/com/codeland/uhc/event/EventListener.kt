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
import com.codeland.uhc.phase.phases.waiting.AbstractLobby
import com.codeland.uhc.phase.phases.waiting.PvpData
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.*
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
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType
import org.ietf.jgss.GSSManager

class EventListener : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val player = event.player
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		NameManager.updateName(event.player)
		DimensionBar.setPlayerBarDimension(event.player)

		/* lobby spawn */
		if (!playerData.participating) {
			AbstractLobby.onSpawnLobby(event.player)
		}
	}

	@EventHandler
	fun onLogOut(event: PlayerQuitEvent) {
		val player = event.player
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		if (playerData.lobbyPVP.inPvp) {
			PvpData.disablePvp(player)

		} else if (playerData.participating) {
			playerData.offlineZombie = playerData.createZombie(player)
		}
	}

	@EventHandler
	fun onPlayerHurt(event: EntityDamageEvent) {
		val player = event.entity
		if (player !is Player) return
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		if (playerData.participating) {
			/* stuff that happens during the game */
			if (GameRunner.uhc.isEnabled(QuirkType.LOW_GRAVITY) && event.cause == EntityDamageEvent.DamageCause.FALL) {
				event.isCancelled = true

			} else if (event.entity is Player && GameRunner.uhc.isEnabled(QuirkType.DEATHSWAP) && Deathswap.swapTime < Deathswap.IMMUNITY) {
				event.isCancelled = true
			}

		/* prevent damage done to players not playing the game */
		} else if (!playerData.lobbyPVP.inPvp) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		val stack = event.item ?: return

		val summoner = GameRunner.uhc.getQuirk(QuirkType.SUMMONER) as Summoner
		if (summoner.enabled && summoner.onSummon(event)) {
			event.isCancelled = true

		} else if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
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
		// TODO more sophisticated detection of items dropped in lobby pvp
		// this ain't cut it chief
		if (GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			event.entity.ticksLived = 5000
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* dying in lobby pvp */
		if (playerData.lobbyPVP.inPvp) {
			PvpData.allInPvp { pvpPlayer, pvpData ->
				if (pvpData.inPvp) pvpPlayer.sendMessage(event.deathMessage ?: "${player.name} fucking died")
			}

		/* players dying in the game */
		} else if (playerData.participating) {
			/* dying in betrayal chc */
			if (GameRunner.uhc.isEnabled(QuirkType.BETRAYAL)) {
				val killer = player.killer

				/* don't drop if you were killed and team swapped */
				if (killer != null) {
					event.keepInventory = true
					event.drops.clear()

					Betrayal.onPlayerDeath(player.uniqueId, killer.uniqueId)
				}

			/* regular deaths */
			} else if (!(GameRunner.uhc.isVariant(PhaseVariant.GRACE_FORGIVING))) {
				val wasPest = Pests.isPest(player)

				if (GameRunner.uhc.isEnabled(QuirkType.PESTS) && !wasPest) Pests.makePest(player)

				if (playerData.alive) GameRunner.playerDeath(player.uniqueId, player.killer)

				if (Pests.isPest(player)) TeamData.removeFromTeam(player.uniqueId, true)
			}

			/* remove chc specific items from drops */

			if (GameRunner.uhc.isEnabled(QuirkType.HOTBAR)) {
				event.drops.removeAll { itemStack ->
					itemStack.type == Material.BLACK_STAINED_GLASS_PANE && itemStack.itemMeta.displayName == "Unusable Slot"
				}
			}

			if (GameRunner.uhc.isEnabled(QuirkType.PLAYER_COMPASS)) PlayerCompass.filterDrops(event.drops)
		}
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

		/* witch poison nerf */

		val potion = event.entity as? ThrownPotion
		if (potion != null) {
			if (potion.shooter is Witch) {
				/* if this is a posion potion replace with nerfed poison */
				if ((potion.item.itemMeta as PotionMeta).basePotionData.type == PotionType.POISON) {
					potion.item = Brew.createCustomPotion(PotionType.POISON, Material.SPLASH_POTION, "Poison", 150, 0)
				}
			}
		}
	}

	@EventHandler
	fun onPlayerRespawn(event: PlayerRespawnEvent) {
		val player = event.player
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* waiting pvp respawning */
		if (playerData.lobbyPVP.inPvp) {
			PvpData.disablePvp(player)
			event.respawnLocation = AbstractLobby.lobbyLocation(GameRunner.uhc, player)

		/* players respawning who are in the game */
		} else if (playerData.participating) {
			val world = Util.worldFromEnvironment(GameRunner.uhc.defaultEnvironment)
			val location = GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
			if (location != null) event.respawnLocation = location

			/* custom quirk behavior when players start */
			GameRunner.uhc.quirks.forEach { quirk ->
				if (quirk.enabled) quirk.onStart(event.player.uniqueId)
			}

		/* players respawning when they are eliminated */
		} else {
			event.respawnLocation = AbstractLobby.onSpawnLobby(player)
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
			if (Pests.isPest(target)) event.isCancelled = true
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
			CommandItemType.commandItemList.any { commandItem -> commandItem.isItem(stack) } ||
			(GameRunner.uhc.isEnabled(QuirkType.PLAYER_COMPASS) && PlayerCompass.isCompass(stack))
	}

	fun shouldHealthCancelled(player: Player): Boolean {
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		return !GameRunner.uhc.naturalRegeneration && (
			playerData.lobbyPVP.inPvp || (
				playerData.participating &&
				!GameRunner.uhc.isPhase(PhaseType.GRACE) &&
				(!GameRunner.uhc.isEnabled(QuirkType.PESTS) || !Pests.isPest(player))
			)
		)
	}

	@EventHandler
	fun onHealthRegen(event: EntityRegainHealthEvent) {
		/* no regeneration in UHC */
		var player = event.entity

		/* make sure it only applies to players */
		/* make sure it only applies to regeneration due to hunger */
		if (player is Player && event.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED) {
			event.isCancelled = shouldHealthCancelled(player)
		}
	}

	@EventHandler
	fun onFoodLevelChange(event: FoodLevelChangeEvent) {
		val player = event.entity as Player

		/* in any combat situation (lobby pvp, nongrace periods) */
		if (shouldHealthCancelled(player)) {
			val over = (event.foodLevel - 17).coerceAtLeast(0)

			if (event.foodLevel > 17) event.foodLevel = 17
			player.saturation += over

		/* when in lobby there is no hunger */
		} else if (!PlayerData.isParticipating(player.uniqueId)) {
			event.foodLevel = 20
			event.isCancelled = true
		}

		/* players in grace will not have hunger affected at all */
	}

	@EventHandler
	fun onEntityDeath(event: EntityDeathEvent) {
		val killer = event.entity.killer

		/* test if offline zombie was killed */
		val (inventory, experience, uuid) = PlayerData.getZombieData(event.entity)

		/* offline zombie was killed */
		if (experience != -1) {
			val playerData = PlayerData.getPlayerData(uuid)

			/* lose reference to old offline zombie */
			playerData.offlineZombie = null
			event.drops.clear()

			/* betrayal */
			if (GameRunner.uhc.isEnabled(QuirkType.BETRAYAL) && killer != null) {
				Betrayal.onPlayerDeath(uuid, killer.uniqueId)

			} else {
				/* drop they player's inventory and experience */
				inventory.forEach { drop -> event.drops.add(drop) }
				val droppedExperience = experience.coerceAtMost(100)
				event.droppedExp = droppedExperience

				/* player can respawn */
				if (GameRunner.uhc.isVariant(PhaseVariant.GRACE_FORGIVING)) {
					val world = Util.worldFromEnvironment(GameRunner.uhc.defaultEnvironment)

					/* create the player's new zombie somewhere */
					playerData.offlineZombie = playerData.createDefaultZombie(
						uuid,
						GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
						?: Location(world, 0.5, Util.topBlockY(world, 0, 0) + 1.0, 0.5)
					)

				/* player is eliminated */
				} else {
					GameRunner.playerDeath(uuid, killer)
					GameRunner.playerAction(uuid) { player -> AbstractLobby.onSpawnLobby(player) }
				}
			}
		} else {
			if (!GameRunner.uhc.isEnabled(QuirkType.MODIFIED_DROPS) || !ModifiedDrops.onDrop(event.entityType, event.drops)) {
				GameRunner.uhc.quirks.any { quirk ->
					quirk.enabled && quirk.drops != null && quirk.drops.any { dropFix -> dropFix.onDeath(event.entity, killer, event.drops) }
				} ||
				DropFixType.values().any { dropFixType -> dropFixType.dropFix.onDeath(event.entity, killer, event.drops) }
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
						drop.amount = drop.amount * 2
					}
				}
			}
		}
	}

	@EventHandler
	fun onEntityDamageEvent(event: EntityDamageByEntityEvent) {
		val attacker = event.damager
		val defender = event.entity
		val grace = GameRunner.uhc.isPhase(PhaseType.GRACE)
		val pests = GameRunner.uhc.isEnabled(QuirkType.PESTS)

		fun isParticipatingPlayer(entity: Entity): Boolean {
			return entity is Player && PlayerData.isParticipating(entity.uniqueId)
		}

		fun isShootingParticipatingPlayer(entity: Entity): Boolean {
			if (entity !is Projectile) return false
			val shooter = entity.shooter
			return shooter is Player && PlayerData.isParticipating(shooter.uniqueId)
		}

		/* player attacking */
		if (isParticipatingPlayer(attacker)) {
			attacker as Player

			/* another player */
			if (isParticipatingPlayer(defender)) {
				defender as Player

				event.isCancelled = grace || (pests && Pests.isPest(attacker) && Pests.isPest(defender))

			/* a player zombie */
			} else if (PlayerData.isZombie(defender)) {
				event.isCancelled = grace
			}
		/* player shooting */
		} else if (isShootingParticipatingPlayer(attacker)) {
			attacker as Projectile

			/* another player */
			if (isParticipatingPlayer(defender)) {
				defender as Player

				event.isCancelled = grace || (pests && Pests.isPest(attacker.shooter as Player) && Pests.isPest(defender))

			/* a player zombie */
			} else if (PlayerData.isZombie(defender)) {
				event.isCancelled = grace
			}
		}
	}

	@EventHandler
	fun onBlockDrop(event: BlockDropItemEvent) {
		val blockState = event.blockState
		val block = event.block
		val type = blockState.type

		val player = event.player
		val drops = event.items
		val blockMiddle = block.location.toCenterLocation()

		/* creative mode does not cause blocks to drop */
		if (event.player.gameMode != GameMode.CREATIVE) {
			if (GameRunner.uhc.isEnabled(QuirkType.ABUNDANCE)) {
				Abundance.replaceDrops(player, block, blockState, drops)

			} else {
				BlockFixType.values().any { blockFixType ->
					blockFixType.blockFix.onBreakBlock(GameRunner.uhc, type, drops, player) { drop ->
						if (drop != null) block.world.dropItem(blockMiddle, drop)
					}
				}
			}
		}
	}

	@EventHandler
	fun onBreakBlock(event: BlockBreakEvent) {
		var block = event.block
		var player = event.player
		var baseItem = event.player.inventory.itemInMainHand

		/* prevent breaking the border in creative lobby */
		val x = GameRunner.uhc.lobbyX
		val z = GameRunner.uhc.lobbyZ
		val radius = GameRunner.uhc.lobbyRadius + 1

		if (((block.x == x + radius || block.x == x - radius) &&
				(block.z > z - radius) && (block.z < z + radius)) ||
			((block.z == z + radius || block.z == z - radius) &&
				(block.x > x - radius) && (block.x < x + radius)) ||
			((block.y == 255 || block.y == 0) && block.x <= x + radius && block.z <= z + radius && block.x >= x - radius && block.z >= z - radius)
		) {
			event.isCancelled = true

		} else if (GameRunner.uhc.isEnabled(QuirkType.UNSHELTERED) && !Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
			var broken = block.state.getMetadata("broken")

			val oldBlockType = block.type
			val oldData = block.blockData

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
		var nearestDistance = 16.0

		/* find the nearest player within 16 blocks to the decaying leaves */
		Bukkit.getOnlinePlayers().forEach { player ->
			if (PlayerData.isParticipating(player.uniqueId)) {
				val playerLocation = player.location

				if (leavesLocation.world == playerLocation.world) {
					val distance = playerLocation.distance(leavesLocation)

					if (distance < nearestDistance) {
						nearestDistance = distance
						nearestPlayer = player
					}
				}
			}
		}

		val dropPlayer = nearestPlayer

		/* apply applefix to this leaves block for the nearest player */
		if (dropPlayer != null)
			BlockFixType.LEAVES.blockFix.onBreakBlock(leavesType, dropPlayer) { drop ->
				if (drop != null) leavesLocation.world.dropItem(event.block.location.toCenterLocation(), drop)
			}
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		val phase = GameRunner.uhc.currentPhase
		val player = event.player
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* things that affect players playing the game */
		if (GameRunner.uhc.isGameGoing() && playerData.participating) {
			/* trying to build above endgame top level */
			if (phase is EndgameNaturalTerrain && event.blockPlaced.y > phase.topBoundary) {
				event.player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Height limit for building is ${phase.topBoundary}")
				event.isCancelled = true

			/* creative block replenishing */
			} else if (GameRunner.uhc.isEnabled(QuirkType.CREATIVE)) {
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

			/* unsheltered block place prevention */
			} else if (GameRunner.uhc.isEnabled(QuirkType.UNSHELTERED)) {
				var block = event.block

				if (!Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
					event.isCancelled = true
				}
			}
		}

		/* things that affect lobby pvp players */
		else if (playerData.lobbyPVP.inPvp) {
			if (event.blockPlaced.y > GameRunner.uhc.lobbyPvpHeight) {
				event.player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Height limit for building is ${GameRunner.uhc.lobbyPvpHeight}")
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
