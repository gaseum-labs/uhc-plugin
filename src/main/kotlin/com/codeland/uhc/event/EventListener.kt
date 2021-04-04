package com.codeland.uhc.event

import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.dropFix.DropFixType
import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.phase.DimensionBar
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.phase.phases.endgame.EndgameNaturalTerrain
import com.codeland.uhc.phase.phases.waiting.AbstractLobby
import com.codeland.uhc.phase.phases.waiting.PvpData
import com.codeland.uhc.quirk.HorseQuirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.team.HideManager
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType

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

		/* update who the player sees */
		HideManager.updateAllForPlayer(player)
		/* update who sees the player */
		HideManager.updatePlayerForAll(player)
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
		/* items despawn faster in PVP and Lobby */
		if (WorldManager.isNonGameWorld(event.entity.world)) event.entity.ticksLived = 5000
	}

	@EventHandler
	fun onWorld(event: PlayerChangedWorldEvent) {
		/* hide this player for everyone else not in the player's new world */
		HideManager.updatePlayerForAll(event.player)

		/* hide other players not in the player's new world to the player */
		HideManager.updateAllForPlayer(event.player)
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* dying in lobby pvp */
		if (playerData.lobbyPVP.inPvp) {
			/* announce death to only lobby pvpers */
			val deathMessage = event.deathMessage()
			if (deathMessage != null) PvpData.allInPvp { pvpPlayer, _ ->
				pvpPlayer.sendMessage(deathMessage)
			}

			/* lobby pvp players do not drop items */
			event.drops.clear()

			/* award killstreak */
			val killer = player.killer ?: return
			PvpData.onKill(killer)

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
			if (GameRunner.uhc.isEnabled(QuirkType.HOTBAR)) Hotbar.filterDrops(event.drops)
			if (GameRunner.uhc.isEnabled(QuirkType.PLAYER_COMPASS)) PlayerCompass.filterDrops(event.drops)
			if (GameRunner.uhc.isEnabled(QuirkType.INFINITE_INVENTORY)) InfiniteInventory.modifyDrops(event.drops, event.entity)
		}
	}

	@EventHandler
	fun onWeather(event: WeatherChangeEvent) {
		event.isCancelled = WorldManager.isNonGameWorld(event.world)
	}

	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
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

		/* pvp respawns back into pvp */
		if (playerData.lobbyPVP.inPvp) {
			event.respawnLocation = PvpData.enablePvp(player, false, false)

		/* players respawning who are in the game */
		} else if (playerData.participating) {
			val respawnLocation = GameRunner.respawnPlayer(player, player.uniqueId, playerData)
			if (respawnLocation != null) event.respawnLocation = respawnLocation

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

				/* player can respawn only in grace forgiving */
				if (GameRunner.uhc.isVariant(PhaseVariant.GRACE_FORGIVING))
					GameRunner.respawnPlayer(null, uuid, playerData)
				else
					GameRunner.playerDeath(uuid, killer)
			}
		} else {
			/* find a quirk that has a dropfix for this entity */
			/* if not fallback to default list of dropfixes */
			(GameRunner.uhc.quirks.filter { quirk ->
				quirk.enabled && quirk.customDrops != null
			}.map { quirk ->
				Util.binaryFind(event.entityType, quirk.customDrops!!) { dropFix -> dropFix.entityType }
			}.firstOrNull()
				?: Util.binaryFind(event.entityType, DropFixType.list) { dropFixType -> dropFixType.dropFix.entityType }?.dropFix
			)?.onDeath(event.entity, killer, event.drops)

			GameRunner.uhc.quirks.any { quirk ->
				quirk.enabled && quirk.modifyEntityDrops(event.entity, killer, event.drops)
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
			BlockFixType.values().any { blockFixType ->
				blockFixType.blockFix.onBreakBlock(GameRunner.uhc, type, drops, player) { drop ->
					if (drop != null) block.world.dropItem(blockMiddle, drop)
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
		/*val x = 0
		val z = 0
		val radius = GameRunner.uhc.lobbyRadius + 1

		if (((block.x == x + radius || block.x == x - radius) &&
				(block.z > z - radius) && (block.z < z + radius)) ||
			((block.z == z + radius || block.z == z - radius) &&
				(block.x > x - radius) && (block.x < x + radius)) ||
			((block.y == 255 || block.y == 0) && block.x <= x + radius && block.z <= z + radius && block.x >= x - radius && block.z >= z - radius)
		) {
			event.isCancelled = true
		*/
		if (GameRunner.uhc.isEnabled(QuirkType.UNSHELTERED) && !Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
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

		val playerData = PlayerData.getPlayerData(player.uniqueId)

		if (playerData.lobbyPVP.inPvp && event.block.y < GameRunner.uhc.lobbyPVPMin) {
			event.player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Low limit for building is ${GameRunner.uhc.lobbyPVPMin}")
			event.isCancelled = true
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
		var nearestDistance = 32.0

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
			if (phase is EndgameNaturalTerrain && event.blockPlaced.y > phase.max) {
				event.player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Height limit for building is ${phase.max}")
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
			if (event.blockPlaced.y > GameRunner.uhc.lobbyPVPMax) {
				event.player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Height limit for building is ${GameRunner.uhc.lobbyPVPMax}")
				event.isCancelled = true

			} else if (event.blockPlaced.y < GameRunner.uhc.lobbyPVPMin) {
				event.player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}Low limit for building is ${GameRunner.uhc.lobbyPVPMin}")
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
		if (GameRunner.uhc.isEnabled(QuirkType.INFINITE_INVENTORY) && event.clickedInventory?.type == InventoryType.PLAYER) {
			val player = event.whoClicked as Player
			if (event.slot == InfiniteInventory.BACK_BUTTON) {
				InfiniteInventory.prevPage(player)
				event.isCancelled = true
			} else if (event.slot == InfiniteInventory.FORWARD_BUTTON) {
				InfiniteInventory.nextPage(player)
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onPickUpItem(event: EntityPickupItemEvent) {
		/* prevent piglins from wearing their bartered boots */
		if (event.entity is Piglin && event.item.itemStack.type != Material.GOLD_INGOT) {
			event.isCancelled = true

		} else if (
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

	@EventHandler
	fun entityDamageA(event: EntityDamageEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.HORSE)) {
			val horse = event.entity as? Horse ?: return
			val playerUUID = HorseQuirk.horseMap[horse.uniqueId] ?: return

			event.isCancelled = true

			GameRunner.damagePlayer(playerUUID, event.finalDamage)
		}
	}

	@EventHandler
	fun onSneak(event: PlayerToggleSneakEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.HORSE)) {
			val player = event.player

			val horseUUID = HorseQuirk.horseMap.asIterable().find { (_, playerUUID) ->
				player.uniqueId == playerUUID
			}?.key ?: return

			val horse = Bukkit.getEntity(horseUUID) as Horse? ?: return

			horse.addPassenger(player)
			event.isCancelled = true
		}
	}
}
