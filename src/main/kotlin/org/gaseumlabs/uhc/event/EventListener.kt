package org.gaseumlabs.uhc.event

import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.gaseumlabs.uhc.blockfix.BlockFixType
import org.gaseumlabs.uhc.core.phase.phases.Endgame
import org.gaseumlabs.uhc.core.phase.phases.Grace
import org.gaseumlabs.uhc.discord.storage.DiscordStorage
import org.gaseumlabs.uhc.dropFix.DropFixType
import org.gaseumlabs.uhc.gui.CommandItemType
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.arena.PvpArena
import org.gaseumlabs.uhc.chc.chcs.*
import org.gaseumlabs.uhc.team.HideManager
import org.gaseumlabs.uhc.team.NameManager
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.Material.ENDER_EYE
import org.bukkit.Material.TNT
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.core.phase.PhaseType.GRACE
import org.gaseumlabs.uhc.core.phase.PhaseType.POSTGAME
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena

class EventListener : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(org.gaseumlabs.uhc.UHCPlugin.plugin) {
			val player = event.player
			val playerData = PlayerData.get(player.uniqueId)

			NameManager.onPlayerLogin(event.player, UHC.getTeams().playersTeam(player.uniqueId))

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
		val playerData = PlayerData.get(player.uniqueId)
		val pvpGame = ArenaManager.playersArena(player.uniqueId)

		if (pvpGame != null) {
			ArenaManager.removePlayer(player.uniqueId)
		} else if (playerData.participating && player.gameMode != GameMode.SPECTATOR) {
			OfflineZombie.createZombie(player, playerData)
		}
	}

	@EventHandler
	fun onSave(event: WorldSaveEvent) {
		if (event.world === WorldManager.pvpWorld) {
			ArenaManager.saveWorldInfo(event.world)
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		val stack = event.item ?: return
		val player = event.player

		if (
			!(
				(event.action === Action.RIGHT_CLICK_AIR || event.action === Action.RIGHT_CLICK_BLOCK) &&
				CommandItemType.values().find { it.isItem(stack) }?.execute(player) != null
			) &&
			stack.type === ENDER_EYE &&
			(event.action === Action.RIGHT_CLICK_AIR || event.action === Action.RIGHT_CLICK_BLOCK) &&
			PlayerData.get(player).alive
		) {
			val playerTeam = UHC.getTeams().playersTeam(player.uniqueId)

			event.isCancelled = true

			val nearPlayer = PlayerData.playerDataList.filter { (_, data) -> data.alive }
				.mapNotNull { (uuid) ->
					val otherPlayer = Bukkit.getPlayer(uuid) ?: return@mapNotNull null
					val team = UHC.getTeams().playersTeam(uuid) ?: return@mapNotNull null
					if (otherPlayer === player || otherPlayer.world !== player.world || team === playerTeam) {
						return@mapNotNull null
					}
					player
				}
				.minByOrNull { player.location.distance(it.location) }
				?: return

			stack.amount -= 1

			val eye = event.player.world.spawnEntity(
				event.player.location.add(0.0, 1.0, 0.0),
				EntityType.ENDER_SIGNAL)
			as EnderSignal
			eye.targetLocation = nearPlayer.location
			eye.setItem(ItemStack(Material.COMPASS))
			eye.dropItem = false

			player.playSound(player.location, Sound.ENTITY_ENDER_EYE_LAUNCH, 1.0f, 1.0f)
		}
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
			location.world.spawnParticle(Particle.REDSTONE,
				location.clone().add(0.0, 1.0, 0.0),
				64,
				0.5,
				1.0,
				0.5,
				Particle.DustOptions(Color.RED, 2.0f))
		}

		val player = event.entity
		val uuid = player.uniqueId
		val playerData = PlayerData.get(uuid)
		val arena = ArenaManager.playersArena(uuid)

		/* dying in lobby pvp */
		if (arena is PvpArena) {
			/* player spectates at that exact place */
			event.isCancelled = true
			bloodCloud(player.location)

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
			bloodCloud(player.location)

			player.gameMode = GameMode.SPECTATOR

		} else if (playerData.participating) {
			event.isCancelled = true
			bloodCloud(player.location)

			/* drop items */
			event.drops.forEach { drop ->
				player.location.world.dropItem(player.location, drop)
			}

			/* drop experience */
			val orb = player.location.world.spawnEntity(player.location, EntityType.EXPERIENCE_ORB) as ExperienceOrb
			orb.experience = event.droppedExp

			val deathMessage = event.deathMessage()
			if (deathMessage != null) Bukkit.getOnlinePlayers()
				.filter { !WorldManager.isNonGameWorld(it.world) }
				.forEach { it.sendMessage(deathMessage) }

			UHC.game?.playerDeath(uuid, player.killer, playerData, false)
		}
	}

	@EventHandler
	fun onWeather(event: WeatherChangeEvent) {
		event.isCancelled = WorldManager.isNonGameWorld(event.world)
	}

	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
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

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		/* offline zombie targeting */
		val target = event.target ?: return

		if (OfflineZombie.getZombieUUID(target) != null) {
			event.isCancelled = true

		} else if (target is Player) {
			if (event.entityType === EntityType.BLAZE) {
				if (event.entity.location.distance(target.location) >= 24) event.isCancelled = true
			}
		}
	}

	@EventHandler
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

	@EventHandler
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

	@EventHandler
	fun onHealthExhaust(event: EntityExhaustionEvent) {
		if (
			event.exhaustionReason == EntityExhaustionEvent.ExhaustionReason.REGEN &&
			shouldHealthCancelled(event.entity as Player)
		) {
			event.isCancelled = true
		}
	}

	@EventHandler
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

	@EventHandler
	fun onEntityDeath(event: EntityDeathEvent) {
		val game = UHC.game ?: return
		val killer = event.entity.killer
		val zombieData = OfflineZombie.getZombieData(event.entity)

		/* offline zombie was killed */
		if (zombieData != null) {
			val (inventory, experience, uuid) = zombieData
			val playerData = PlayerData.get(uuid)

			playerData.offlineZombie = null

			/* drop they player's inventory and experience */
			event.drops.clear()
			inventory.forEach { drop -> event.drops.add(drop) }
			val droppedExperience = experience.coerceAtMost(100)
			event.droppedExp = droppedExperience

			game.playerDeath(uuid, killer, playerData, false)

		} else {
			game.chc?.customDrops?.let { list -> Util.binaryFind(event.entityType, list) { it.entityType } }
				?: Util.binaryFind(event.entityType, DropFixType.list) { it.dropFix.entityType }
				?.dropFix?.onDeath(event.entity, killer, event.drops)

			game.chc?.modifyEntityDrops(event.entity, killer, event.drops)
		}
	}

	/**
	 * prevents damage dealt to players in protected times
	 */
	@EventHandler
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
	@EventHandler
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

	@EventHandler
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

	@EventHandler
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

	@EventHandler
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

	@EventHandler
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

	@EventHandler
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

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		val block = event.clickedBlock ?: return
		if (
			event.action === Action.RIGHT_CLICK_BLOCK &&
			block.type === Material.RESPAWN_ANCHOR &&
			block.world === WorldManager.lobbyWorld
		) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onPickUpItem(event: EntityPickupItemEvent) {
		/* prevent piglins from wearing their bartered boots */
		if (event.entity is Piglin && event.item.itemStack.type != Material.GOLD_INGOT) {
			event.isCancelled = true
		}
	}

	@EventHandler
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

	@EventHandler
	fun onGrow(event: BlockGrowEvent) {
		if (
			event.newState.type === Material.SUGAR_CANE &&
			WorldManager.isGameWorld(event.newState.world)
		) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onDrinkPotion(event: EntityPotionEffectEvent) {
		/* this shit doesn't work */
		if (
			event.entityType === EntityType.WANDERING_TRADER
		) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun serverListPing(event: ServerListPingEvent) {
		/* do not attempt to modify MOTD if discordstorage is not set */
		val splashText = DiscordStorage.splashText ?: return

		val color0 = TextColor.color(DiscordStorage.color0 ?: return)
		val color1 = TextColor.color(DiscordStorage.color1 ?: return)

		val length = 48
		val strip =
			String(CharArray(length + 1) { i -> if (i == length) '\n' else splashText[i % splashText.length] })

		event.motd(
			Util.gradientString(strip, color0, color1).append(Util.gradientString(strip, color0, color1))
		)
	}
}
