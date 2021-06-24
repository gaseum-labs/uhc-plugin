package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.core.*
import com.codeland.uhc.dropFix.DropFixType
import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.phases.endgame.EndgameNaturalTerrain
import com.codeland.uhc.quirk.HorseQuirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.team.HideManager
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.event.vehicle.VehicleCreateEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType
import java.util.*

class EventListener : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
			val player = event.player
			val playerData = PlayerData.getPlayerData(player.uniqueId)

			NameManager.updateName(event.player)

			/* lobby spawn */
			if (!playerData.participating) {
				Lobby.onSpawnLobby(event.player)
			}

			/* update who the player sees */
			HideManager.updateAllForPlayer(player)
			/* update who sees the player */
			HideManager.updatePlayerForAll(player)
		}
	}

	@EventHandler
	fun onLogOut(event: PlayerQuitEvent) {
		val player = event.player
		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val pvpGame = PvpGameManager.playersGame(player.uniqueId)

		if (pvpGame != null) {
			PvpGameManager.disablePvp(player)

		} else if (playerData.participating && player.gameMode != GameMode.SPECTATOR) {
			playerData.offlineZombie = playerData.createZombie(player)
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		val stack = event.item ?: return

		val summoner = UHC.getQuirk(QuirkType.SUMMONER) as Summoner
		if (summoner.enabled.get() && summoner.onSummon(event)) {
			event.isCancelled = true

		} else if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
			CommandItemType.commandItemList.any { commandItem ->
				if (commandItem.isItem(stack)) {
					commandItem.onUse(UHC, event.player)
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
		fun bloodCloud(location: Location) {
			location.world.spawnParticle(Particle.REDSTONE, location.clone().add(0.0, 1.0, 0.0), 64, 0.5, 1.0, 0.5, Particle.DustOptions(Color.RED, 2.0f))
		}

		val player = event.entity
		val uuid = player.uniqueId
		val playerData = PlayerData.getPlayerData(uuid)
		val pvpGame = PvpGameManager.playersGame(uuid)

		/* dying in lobby pvp */
		if (pvpGame != null) {
			/* player spectates at that exact place */
			event.isCancelled = true
			bloodCloud(player.location)

			player.gameMode = GameMode.SPECTATOR

			/* announce death to only pvp game players */
			val deathMessage = event.deathMessage()
			if (deathMessage != null) pvpGame.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { pvpPlayer ->
				pvpPlayer.sendMessage(deathMessage)
			}

			pvpGame.checkEnd()

		/* players dying in the game */
		} else if (playerData.participating) {
			event.isCancelled = true
			bloodCloud(player.location)

			/* remove chc specific items from drops */
			if (UHC.isEnabled(QuirkType.HOTBAR)) Hotbar.filterDrops(event.drops)
			if (UHC.isEnabled(QuirkType.PLAYER_COMPASS)) PlayerCompass.filterDrops(event.drops)
			if (UHC.isEnabled(QuirkType.INFINITE_INVENTORY)) InfiniteInventory.modifyDrops(event.drops, event.entity)

			/* drop items */
			event.drops.forEach { drop ->
				player.location.world.dropItem(player.location, drop)
			}

			/* drop experience */
			val orb = player.location.world.spawnEntity(player.location, EntityType.EXPERIENCE_ORB) as ExperienceOrb
			orb.experience = event.droppedExp

			val deathMessage = event.deathMessage()
			if (deathMessage != null) Bukkit.getOnlinePlayers().filter { !WorldManager.isNonGameWorld(it.world) }.forEach { player ->
				player.sendMessage(deathMessage)
			}

			GameRunner.playerDeath(uuid, player.killer, playerData, false)
		}
	}

	@EventHandler
	fun onWeather(event: WeatherChangeEvent) {
		event.isCancelled = WorldManager.isNonGameWorld(event.world)
	}

	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
		if (event.entity.world.name == WorldManager.LOBBY_WORLD_NAME) {
			event.isCancelled = true

		/* witch poison nerf */
		} else if (!UHC.naturalRegeneration.get()) {
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
	}

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		/* offline zombie targeting */
		val target = event.target ?: return

		if (PlayerData.isZombie(target) && (event.entity.type == EntityType.IRON_GOLEM || event.entity.type == EntityType.SNOWMAN)) {
			event.isCancelled = true

		} else if (target is Player) {
			val summoner = UHC.getQuirk(QuirkType.SUMMONER) as Summoner
			if (summoner.enabled.get() && summoner.commander.get()) {
				val team = TeamData.playersTeam(target.uniqueId)

				if (team != null && Summoner.isCommandedBy(event.entity, team)) {
					event.isCancelled = true
				}
			}

			if (UHC.isEnabled(QuirkType.PESTS) && PlayerData.isUndead(target.uniqueId)) {
				event.isCancelled = true
			}

			if (event.entityType === EntityType.BLAZE) {
				val distance = event.entity.location.distance(target.location)

				if (distance >= 24) event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		val player = event.whoClicked

		/* prevent pest crafting */
		if (UHC.isEnabled(QuirkType.PESTS) && PlayerData.isUndead(player.uniqueId)) {
			if (Util.binarySearch(event.recipe.result.type, Pests.banList)) event.isCancelled = true

		} else {
			val type = event.currentItem?.type ?: return

			event.currentItem = when (type) {
				Material.STONE_AXE -> AxeFix.stoneAxe()
				Material.IRON_AXE -> AxeFix.ironAxe()
				Material.DIAMOND_AXE -> AxeFix.diamondAxe()
				else -> event.currentItem
			}
		}
	}

	@EventHandler
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		val stack = event.itemDrop.itemStack

		event.isCancelled =
			CommandItemType.commandItemList.any { commandItem -> commandItem.isItem(stack) } ||
			(UHC.isEnabled(QuirkType.PLAYER_COMPASS) && PlayerCompass.isCompass(stack))
	}

	fun shouldHealthCancelled(player: Player): Boolean {
		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val pvpGame = PvpGameManager.playersGame(player.uniqueId)

		return !UHC.naturalRegeneration.get() && (
			pvpGame != null || (
				playerData.participating &&
				!UHC.isPhase(PhaseType.GRACE) &&
				!(UHC.isEnabled(QuirkType.PESTS) && playerData.undead())
			)
		)
	}

	@EventHandler
	fun onHealthRegen(event: EntityRegainHealthEvent) {
		/* no regeneration in UHC */
		val player = event.entity

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

			/* drop they player's inventory and experience */
			inventory.forEach { drop -> event.drops.add(drop) }
			val droppedExperience = experience.coerceAtMost(100)
			event.droppedExp = droppedExperience

			GameRunner.playerDeath(uuid, killer, playerData, false)

		} else {
			/* find a quirk that has a dropfix for this entity */
			/* if not fallback to default list of dropfixes */
			(UHC.quirks.filter { quirk ->
				quirk.enabled.get() && quirk.customDrops != null
			}.map { quirk ->
				Util.binaryFind(event.entityType, quirk.customDrops!!) { dropFix -> dropFix.entityType }
			}.firstOrNull()
				?: Util.binaryFind(event.entityType, DropFixType.list) { dropFixType -> dropFixType.dropFix.entityType }?.dropFix
			)?.onDeath(event.entity, killer, event.drops)

			UHC.quirks.any { quirk ->
				quirk.enabled.get() && quirk.modifyEntityDrops(event.entity, killer, event.drops)
			}
		}
	}

	/**
	 * prevents damage dealt to players in protected times
	 */
	@EventHandler
	fun entityDamage(event: EntityDamageEvent) {
		val player = event.entity as? Player ?: return
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		///* stuff that happens during the game */
		if (playerData.participating) {
			if (UHC.isEnabled(QuirkType.LOW_GRAVITY) && event.cause == EntityDamageEvent.DamageCause.FALL) {
				event.isCancelled = true

			} else if (event.entity is Player && UHC.isEnabled(QuirkType.DEATHSWAP) && Deathswap.swapTime < Deathswap.IMMUNITY) {
				event.isCancelled = true
			}

		//* prevent lobby and postgame damage */
		} else {
			val pvpGame = PvpGameManager.playersGame(player.uniqueId)

			if ((pvpGame != null && pvpGame.isOver()) || pvpGame == null) event.isCancelled = true
		}
	}

	/**
	 * prevents combat during grace or between pests
	 */
	@EventHandler
	fun entityDamageByEntity(event: EntityDamageByEntityEvent) {
		val attacker = event.damager
		val defender = event.entity

		val attackingPlayer = when (attacker) {
			is Player -> attacker
			is Projectile -> attacker.shooter as? Player
			else -> null
		}

		if ((
			attackingPlayer != null &&
			PlayerData.isParticipating(attackingPlayer.uniqueId) &&
			defender is Player &&
			PlayerData.isParticipating(defender.uniqueId)
		) && (
			UHC.isPhase(PhaseType.GRACE) || (
				UHC.isEnabled(QuirkType.PESTS) &&
				!PlayerData.isAlive(attackingPlayer.uniqueId) &&
				!PlayerData.isAlive(defender.uniqueId)
			)
		)) event.isCancelled = true
	}

	/**
	 * prevent pearl damage
	 */
	@EventHandler
	fun onPlayerPearl(event: PlayerTeleportEvent) {
		if (event.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
			event.isCancelled = true
			event.player.noDamageTicks = 1
			event.player.teleport(event.to)
		}
	}

	@EventHandler
	fun onBlockDrop(event: BlockDropItemEvent) {
		val blockState = event.blockState
		val block = event.block
		val type = blockState.type

		val player = event.player
		val drops = event.items

		/* creative mode does not cause blocks to drop */
		if (event.player.gameMode != GameMode.CREATIVE) {
			BlockFixType.values().any { blockFixType ->
				blockFixType.blockFix.onBreakBlock(type, drops, player) { drop ->
					if (drop != null) block.world.dropItemNaturally(block.location, drop)
				}
			}
		}
	}

	private val hoes = arrayOf(
		Material.SHEARS,
		Material.WOODEN_HOE,
		Material.GOLDEN_HOE,
		Material.STONE_HOE,
		Material.IRON_HOE,
		Material.DIAMOND_HOE,
		Material.NETHERITE_HOE,
	)

	@EventHandler
	fun onItemDamage(event: PlayerItemDamageEvent) {
		if (hoes.contains(event.item.type)) {
			event.damage = if (Math.random() < 0.4) 1 else 0
		}
	}

	@EventHandler
	fun onBreakBlock(event: BlockBreakEvent) {
		val block = event.block
		val player = event.player

		if (UHC.isEnabled(QuirkType.UNSHELTERED) && !Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
			val oldBlockType = block.type
			val oldData = block.blockData

			/* block has not been set as broken */
			if (Unsheltered.isBroken(block)) {
				player.sendActionBar(Component.text("Block already broken!", NamedTextColor.GOLD, TextDecoration.BOLD))
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

		val dropPlayer = Bukkit.getOnlinePlayers().filter { PlayerData.isParticipating(it.uniqueId) && it.world === leavesLocation.world }.minByOrNull {
			it.location.distance(leavesLocation)
		}

		/* apply applefix to this leaves block for the nearest player */
		if (dropPlayer != null) BlockFixType.LEAVES.blockFix.onBreakBlock(leavesType, mutableListOf(), dropPlayer) { drop ->
			if (drop != null) leavesLocation.world.dropItemNaturally(event.block.location, drop)
		}
	}

	@EventHandler
	fun onBucket(event: PlayerBucketEmptyEvent) {
		val player = event.player
		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val block = event.block
		val phase = UHC.currentPhase

		if (playerData.participating && phase is EndgameNaturalTerrain && block.y > phase.finalMax) {
			phase.addSkybaseBlock(block)
		}
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		val phase = UHC.currentPhase
		val player = event.player
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* things that affect players playing the game */
		if (UHC.isGameGoing() && playerData.participating) {
			/* trying to build above endgame top level */
			if (phase is EndgameNaturalTerrain && event.blockPlaced.y > phase.finalMax) {
				phase.addSkybaseBlock(event.blockPlaced)

			/* creative block replenishing */
			} else if (UHC.isEnabled(QuirkType.CREATIVE)) {
				val material = event.itemInHand.type

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
			} else if (UHC.isEnabled(QuirkType.UNSHELTERED)) {
				val block = event.block

				if (!Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
					event.isCancelled = true
				}
			}
		} else {
			val pvpGame = PvpGameManager.playersGame(player.uniqueId)

			if (pvpGame != null && event.blockPlaced.y > 127) {
				event.player.sendActionBar(Component.text("Height limit for building is 127", NamedTextColor.RED, TextDecoration.BOLD))
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onVehiclePlace(event: VehicleCreateEvent) {
		if (event.vehicle.world.name == WorldManager.LOBBY_WORLD_NAME) event.isCancelled = true
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		if (UHC.isEnabled(QuirkType.HOTBAR))
			if (event.clickedInventory?.type == InventoryType.PLAYER && event.slot in 9..35) {
				event.isCancelled = true
			}
		if (UHC.isEnabled(QuirkType.INFINITE_INVENTORY) && event.clickedInventory?.type == InventoryType.PLAYER && event.action != InventoryAction.NOTHING) {
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
			UHC.isEnabled(QuirkType.HALLOWEEN) &&
			event.entityType == EntityType.PLAYER &&
			event.item.itemStack.type == Material.DIAMOND &&
			UHC.isGameGoing() &&
			event.entity.name != "balduvian"
		) {
			val halloween = UHC.getQuirk(QuirkType.HALLOWEEN) as Halloween

			if (!halloween.hasGottenDiamonds) {
				Halloween.jumpScare(event.entity as Player)
				halloween.hasGottenDiamonds = true
			}
		}
	}

	@EventHandler
	fun entityDamageA(event: EntityDamageEvent) {
		if (UHC.isEnabled(QuirkType.HORSE)) {
			val horse = event.entity as? Horse ?: return
			val playerUUID = HorseQuirk.horseMap[horse.uniqueId] ?: return

			event.isCancelled = true

			GameRunner.damagePlayer(playerUUID, event.finalDamage)
		}
	}

	@EventHandler
	fun onSneak(event: PlayerToggleSneakEvent) {
		if (UHC.isEnabled(QuirkType.HORSE)) {
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
