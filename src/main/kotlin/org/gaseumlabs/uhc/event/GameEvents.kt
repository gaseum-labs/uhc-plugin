package org.gaseumlabs.uhc.event

import io.papermc.paper.event.block.BlockBreakBlockEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.Material.ENDER_EYE
import org.bukkit.Material.TNT
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.gaseumlabs.uhc.blockfix.BlockFixType
import org.gaseumlabs.uhc.chc.chcs.Pests
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.core.phase.PhaseType.GRACE
import org.gaseumlabs.uhc.core.phase.PhaseType.POSTGAME
import org.gaseumlabs.uhc.core.phase.phases.Endgame
import org.gaseumlabs.uhc.core.phase.phases.Grace
import org.gaseumlabs.uhc.dropFix.DropFixType
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena
import org.gaseumlabs.uhc.lobbyPvp.arena.PvpArena
import org.gaseumlabs.uhc.util.Action.getPlayerLocation
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.util.Action.sendGameMessage

/**
 * Events should have low priority
 * so they can be overwritten by CHC events
 */
class GameEvents : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onUseItem(event: PlayerInteractEvent) {
		val stack = event.item ?: return
		val player = event.player

		if (
			(event.action === Action.RIGHT_CLICK_AIR || event.action === Action.RIGHT_CLICK_BLOCK) &&
			stack.type === ENDER_EYE &&
			PlayerData.get(player).alive
		) {
			event.isCancelled = true

			val playerTeam = UHC.getTeams().playersTeam(player.uniqueId)

			val findLoc = PlayerData.playerDataList.filter { (uuid, data) ->
				data.alive && UHC.getTeams().playersTeam(uuid) !== playerTeam
			}
				.mapNotNull { (uuid) -> getPlayerLocation(uuid) }
				.filter { it.world === event.player.world }
				.minByOrNull { player.location.distance(it) }
				?: return sendGameMessage(player, "No one is around")

			stack.amount -= 1

			val eye = event.player.world.spawnEntity(
				event.player.eyeLocation.add(event.player.location.direction),
				EntityType.ENDER_SIGNAL
			) as EnderSignal
			eye.targetLocation = findLoc
			eye.setItem(ItemStack(Material.COMPASS))
			eye.dropItem = false

			player.playSound(player.location, Sound.ENTITY_ENDER_EYE_LAUNCH, 1.0f, 1.0f)
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onWeather(event: WeatherChangeEvent) {
		event.isCancelled = WorldManager.isNonGameWorld(event.world)
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onEntitySpawn(event: EntitySpawnEvent) {

		if (event.entity.entitySpawnReason === CreatureSpawnEvent.SpawnReason.NETHER_PORTAL) {

		}

		if (event.entity.entitySpawnReason === CreatureSpawnEvent.SpawnReason.REINFORCEMENTS) {
			event.isCancelled = true

		} else if (UHC.game?.config?.naturalRegeneration == false && WorldManager.isGameWorld(event.entity.world)) {
			val potion = event.entity as? ThrownPotion

			if (
				potion != null &&
				potion.shooter is Witch &&
				(potion.item.itemMeta as PotionMeta).basePotionData.type == PotionType.POISON
			) {
				potion.item =
					Brew.createCustomPotion(PotionType.POISON, Material.SPLASH_POTION, "Poison", 150, 0).create()
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		/* offline zombie targeting */
		val target = event.target ?: return

		if (OfflineZombie.getZombieData(target) != null) {
			event.isCancelled = true

		} else if (target is Player) {
			if (event.entityType === EntityType.BLAZE) {
				if (event.entity.location.distance(target.location) >= 24) event.isCancelled = true
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onCraft(event: CraftItemEvent) {
		if (
			PlayerData.get(event.whoClicked).participating && (
				event.recipe.result.type === Material.END_CRYSTAL ||
				event.recipe.result.type === Material.RESPAWN_ANCHOR
			)
		)
			event.currentItem = ItemStack(TNT, 3)
	}

	fun shouldHealthCancelled(player: Player): Boolean {
		val playerData = PlayerData.get(player.uniqueId)
		val game = UHC.game

		return ArenaManager.playersArena(player.uniqueId) is PvpArena || (
			game != null &&
			game.phase !is Grace &&
			!game.config.naturalRegeneration &&
			playerData.participating &&
			!(game.chc is Pests && playerData.undead())
		)
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onHealthRegen(event: EntityRegainHealthEvent) {
		/* no regeneration in UHC */
		val player = event.entity as? Player ?: return

		/* make sure it only applies to players */
		/* make sure it only applies to regeneration due to hunger */
		if (
			event.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED &&
			shouldHealthCancelled(player)
		) {
			event.isCancelled = true
		} else {
			/* health total freezes after the pvp game */
			/* also freezes after the uhc game */
			val arena = ArenaManager.playersArena(player.uniqueId)

			if (
				(arena as? PvpArena)?.isOver() == true ||
				(UHC.game?.phase?.phaseType === POSTGAME && player.world === WorldManager.gameWorld)
			) {
				event.isCancelled = true
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onHealthExhaust(event: EntityExhaustionEvent) {
		if (
			event.exhaustionReason == EntityExhaustionEvent.ExhaustionReason.REGEN &&
			shouldHealthCancelled(event.entity as Player)
		) {
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onFoodLevelChange(event: FoodLevelChangeEvent) {
		/* players in the lobby or in postgame, excluding those in lobby pvp */
		/* they don't lose food */
		if (
			!shouldHealthCancelled(event.entity as Player) &&
			!PlayerData.get(event.entity).participating
		) {
			event.foodLevel = 20
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onEntityDeath(event: EntityDeathEvent) {
		val game = UHC.game ?: return
		val killer = event.entity.killer
		val zombieData = OfflineZombie.getZombieData(event.entity)

		/* offline zombie was killed */
		if (zombieData != null) {
			Game.bloodCloud(event.entity.location)

			val (uuid, inventory, experience) = zombieData
			val playerData = PlayerData.get(uuid)

			playerData.offlineZombie = null

			/* drop they player's inventory and experience */
			event.drops.clear()
			inventory.forEach { drop -> event.drops.add(drop) }
			val droppedExperience = experience.coerceAtMost(100)
			event.droppedExp = droppedExperience

			game.playerDeath(uuid, event.entity.location, killer, playerData, false)

		} else {
			Util.binaryFind(event.entityType, DropFixType.list) { it.dropFix.entityType }
				?.dropFix?.onDeath(event.entity, killer, event.drops)
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity
		val uuid = player.uniqueId
		val playerData = PlayerData.get(uuid)
		val arena = ArenaManager.playersArena(uuid)

		/* dying in lobby pvp */
		if (arena is PvpArena) {
			/* player spectates at that exact place */
			event.isCancelled = true
			Game.bloodCloud(player.location)

			player.gameMode = GameMode.SPECTATOR

			/* announce death to only pvp game players */
			val deathMessage = event.deathMessage()
			if (deathMessage != null) arena.online().forEach { pvpPlayer ->
				pvpPlayer.sendMessage(deathMessage)
			}

			arena.checkEnd()

			/* players dying in the game */
		} else if (arena is GapSlapArena) {
			event.isCancelled = true
			Game.bloodCloud(player.location)

			player.gameMode = GameMode.SPECTATOR

		} else if (playerData.participating) {
			event.isCancelled = true
			Game.bloodCloud(player.location)

			/* drop items */
			event.drops.forEach { drop ->
				player.location.world.dropItem(player.location, drop)
			}

			/* drop experience */
			val orb = player.location.world.spawnEntity(player.location, EntityType.EXPERIENCE_ORB) as ExperienceOrb
			orb.experience = event.droppedExp

			event.deathMessage()?.let { deathMessage ->
				PlayerData.playerDataList.filter { (_, data) -> data.participating }
					.mapNotNull { (uuid, _) -> Bukkit.getPlayer(uuid) }
					.forEach { player -> player.sendMessage(deathMessage) }
			}

			UHC.game?.playerDeath(uuid, player.location, player.killer, playerData, false)
		}
	}

	/**
	 * prevents damage dealt to players in protected times
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	fun entityDamage(event: EntityDamageEvent) {
		val player = event.entity as? Player ?: return
		val playerData = PlayerData.get(player.uniqueId)

		/* stuff that happens during the game */
		if (playerData.participating) {
			val game = UHC.game ?: return

			if (
				game.phase.phaseType === GRACE &&
				(event.cause === LAVA || event.cause === FIRE || event.cause === FIRE_TICK)
			)
				event.isCancelled = true

			/* prevent lobby and postgame damage */
		} else {
			val arena = ArenaManager.playersArena(player.uniqueId)

			if ((arena as? PvpArena)?.isOver() == true || arena == null) {
				event.isCancelled = true
			}
		}
	}

	/**
	 * prevents combat during grace or between pests
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	fun entityDamageByEntity(event: EntityDamageByEntityEvent) {
		val game = UHC.game ?: return

		val attacker = event.damager
		val defender = event.entity

		val attackingPlayer = when (attacker) {
			is Player -> attacker
			is Projectile -> attacker.shooter as? Player
			else -> null
		}

		val attackingData = attackingPlayer?.let(PlayerData::get) ?: return
		val defendingData = if (defender !is Player) return else defender.let(PlayerData::get)

		if (
			(
				attackingData.participating &&
				defendingData.participating
			) && (
				game.phase is Grace || (
					game.chc is Pests && !attackingData.alive && !defendingData.alive
				)
			)
		) event.isCancelled = true
	}

	/**
	 * prevent pearl damage
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerPearl(event: PlayerTeleportEvent) {
		if (event.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
			event.isCancelled = true
			event.player.noDamageTicks = 1
			event.player.teleport(event.to)
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onBlockDrop(event: BlockDropItemEvent) {
		val blockState = event.blockState
		val block = event.block

		val player = event.player
		val drops = event.items

		/* creative mode does not cause blocks to drop */
		if (event.player.gameMode != GameMode.CREATIVE) {
			BlockFixType.values().any { blockFixType ->
				blockFixType.blockFix.onBreakBlock(blockState, drops, player) { drop ->
					if (drop != null) block.world.dropItemNaturally(block.location, drop)
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onBlockDropEnv(event: BlockBreakBlockEvent) {
		BlockFixType.values().any { blockFixType ->
			blockFixType.blockFix.onNaturalBreakBlock(event.block, event.drops) { drop ->
				if (drop != null) event.block.world.dropItemNaturally(event.block.location, drop)
			}
		}
	}

	private val hoes = arrayOf(
		Material.WOODEN_HOE,
		Material.GOLDEN_HOE,
		Material.STONE_HOE,
		Material.IRON_HOE,
		Material.DIAMOND_HOE,
		Material.NETHERITE_HOE,
	)

	data class HoeCount(var count: Int)

	@EventHandler(priority = EventPriority.LOWEST)
	fun onItemDamage(event: PlayerItemDamageEvent) {
		if (hoes.contains(event.item.type)) {
			val meta = event.player.getMetadata("_U_Hoe")
			val hoeCount = if (meta.isEmpty()) {
				val count = HoeCount(0)
				event.player.setMetadata("_U_Hoe", FixedMetadataValue(org.gaseumlabs.uhc.UHCPlugin.plugin, count))
				count
			} else {
				meta.first().value() as HoeCount
			}

			if (hoeCount.count % 5 == 0 || hoeCount.count % 5 == 2 || hoeCount.count % 5 == 4) {
				event.damage = 0
			}

			++hoeCount.count
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onDecay(event: LeavesDecayEvent) {
		/* prevent default drops */
		event.isCancelled = true

		val leavesLocation = event.block.location.toCenterLocation()

		val dropPlayer = Bukkit.getOnlinePlayers()
			.filter { PlayerData.get(it).participating && it.world === leavesLocation.world }
			.minByOrNull { it.location.distance(leavesLocation) }

		/* apply applefix to this leaves block for the nearest player */
		if (dropPlayer != null) BlockFixType.LEAVES.blockFix.onBreakBlock(
			event.block.state,
			mutableListOf(),
			dropPlayer
		) { drop ->
			if (drop != null) leavesLocation.world.dropItemNaturally(event.block.location, drop)
		}

		/* set after drops so that leaves state is captured */
		event.block.type = Material.AIR
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onBucket(event: PlayerBucketEmptyEvent) {
		val game = UHC.game ?: return
		val phase = game.phase as? Endgame ?: return
		val block = event.block

		if (PlayerData.get(event.player.uniqueId).participating) {
			if (block.y > phase.highLimit + Endgame.BUILD_ADD) {
				event.isCancelled = true
				Commands.errorMessage(event.player, "Height limit for endgame reached")

			} else if (block.y > phase.highLimit) {
				phase.addSkybaseBlock(block)
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlaceBlock(event: BlockPlaceEvent) {
		val game = UHC.game
		val player = event.player
		val playerData = PlayerData.get(player.uniqueId)

		/* things that affect players playing the game */
		if (game != null && playerData.participating) {
			/* trying to build above endgame top level */
			val phase = game.phase
			if (phase is Endgame) {
				val block = event.blockPlaced
				if (block.y > phase.highLimit + Endgame.BUILD_ADD) {
					event.isCancelled = true
					Commands.errorMessage(event.player, "Height limit for endgame reached")

				} else if (block.y > phase.highLimit) {
					phase.addSkybaseBlock(block)
				}
			}
		} else if (
			ArenaManager.playersArena(player.uniqueId) is PvpArena &&
			event.blockPlaced.y > 100
		) {
			event.player.sendActionBar(
				Component.text("Height limit for building is 100",
				NamedTextColor.RED,
				TextDecoration.BOLD)
			)
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onPickUpItem(event: EntityPickupItemEvent) {
		/* prevent piglins from wearing their bartered boots */
		if (event.entity is Piglin && event.item.itemStack.type != Material.GOLD_INGOT) {
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onEat(event: PlayerItemConsumeEvent) {
		if (
			event.item.hasItemMeta() &&
			(event.item.itemMeta as PersistentDataHolder)
				.persistentDataContainer
				.has(KillReward.uhcAppleKey, PersistentDataType.INTEGER)
		) {
			event.isCancelled = true

			val findItem = event.item
			val ateItem = event.player.inventory.find { it == findItem } ?: return
			--ateItem.amount

			event.player.saturation += 9.6f
			event.player.foodLevel += 4

			event.player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, true, true))
			event.player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 2400, 1, false, true, true))
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onGrow(event: BlockGrowEvent) {
		if (
			event.newState.type === Material.SUGAR_CANE &&
			WorldManager.isGameWorld(event.newState.world)
		) {
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onDrinkPotion(event: EntityPotionEffectEvent) {
		/* this shit doesn't work */
		if (
			event.entityType === EntityType.WANDERING_TRADER
		) {
			event.isCancelled = true
		}
	}
}
