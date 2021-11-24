package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.blockfix.BlockFixType
import com.codeland.uhc.core.*
import com.codeland.uhc.dropFix.DropFixType
import com.codeland.uhc.gui.CommandItemType
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.core.phase.phases.Endgame
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.team.HideManager
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldGenOption
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.Material
import org.bukkit.Particle
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
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

class EventListener : Listener {
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
			val player = event.player
			val playerData = PlayerData.getPlayerData(player.uniqueId)

			NameManager.updateName(event.player, UHC.getTeams().playersTeam(player.uniqueId))

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
		val pvpGame = ArenaManager.playersArena(player.uniqueId)

		if (pvpGame != null) {
			ArenaManager.removePlayer(player.uniqueId)

		} else if (playerData.participating && player.gameMode != GameMode.SPECTATOR) {
			playerData.offlineZombie = playerData.createZombie(player)
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

		if ((UHC.game?.getQuirk<Summoner>(QuirkType.SUMMONER))?.onSummon(event) == true) {
			event.isCancelled = true

		} else if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
			CommandItemType.values().any { type ->
				if (type.isItem(stack)) {
					type.execute(event.player)
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
		} else if (playerData.participating) {
			event.isCancelled = true
			bloodCloud(player.location)

			/* remove chc specific items from drops */
			val game = UHC.game
			if (game != null) {
				game.getQuirk<Hotbar>(QuirkType.HOTBAR)?.filterDrops(event.drops)
				game.getQuirk<PlayerCompass>(QuirkType.HOTBAR)?.filterDrops(event.drops)
				game.getQuirk<InfiniteInventory>(QuirkType.HOTBAR)?.filterDrops(event.drops, player)
			}

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

			game?.playerDeath(uuid, player.killer, playerData, false)
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

		} else if (event.entity.world === WorldManager.lobbyWorld) {
			event.isCancelled = true

		/* witch poison nerf */
		} else if (UHC.game?.naturalRegeneration?.get() == true) {
			val potion = event.entity as? ThrownPotion
			if (potion != null) {
				if (potion.shooter is Witch) {
					/* if this is a posion potion replace with nerfed poison */
					if ((potion.item.itemMeta as PotionMeta).basePotionData.type == PotionType.POISON) {
						potion.item = Brew.createCustomPotion(PotionType.POISON, Material.SPLASH_POTION, "Poison", 150, 0).create()
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
			val game = UHC.game
			if (game?.quirkEnabled(QuirkType.SUMMONER) == true) {
				val team = game.teams.playersTeam(target.uniqueId)

				if (team != null && Summoner.isCommandedBy(event.entity, team)) {
					event.isCancelled = true
				}
			}

			if (game?.quirkEnabled(QuirkType.PESTS) == true && PlayerData.isUndead(target.uniqueId)) {
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
		if (UHC.game?.quirkEnabled(QuirkType.PESTS) == true && PlayerData.isUndead(player.uniqueId)) {
			if (Util.binarySearch(event.recipe.result.type, Pests.banList)) event.isCancelled = true

		} else if (
			PlayerData.isParticipating(player.uniqueId) && (
			event.recipe.result.type === Material.END_CRYSTAL ||
			event.recipe.result.type === Material.RESPAWN_ANCHOR
		)) {
			event.isCancelled = true
			event.inventory.matrix.forEach { it.amount = 0 }

			player.closeInventory()
			player.showTitle(Title.title(Component.text("BRUH", TextColor.color(0x791ee8)), Component.empty()))
			player.playSound(Sound.sound(Sound.Type { Key.key("entity.ghast.death") }, Sound.Source.MASTER, 2.0f, 0.5f), Sound.Emitter.self())
		}
	}

	@EventHandler
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		val stack = event.itemDrop.itemStack

		if (
			CommandItemType.values().any { it.isItem(stack) }
			|| (UHC.game?.quirkEnabled(QuirkType.PLAYER_COMPASS) == true && PlayerCompass.isCompass(stack))
		) {
			event.isCancelled = true
		}
	}

	fun shouldHealthCancelled(player: Player): Boolean {
		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val game = UHC.game

		return ArenaManager.playersArena(player.uniqueId) is PvpArena || (
			game != null &&
			!game.config.naturalRegeneration.get() &&
			playerData.participating &&
			game.phase !is Grace &&
			!(game.quirkEnabled(QuirkType.PESTS) && playerData.undead())
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
			val game = ArenaManager.playersArena(player.uniqueId)
			if ((game as? PvpArena)?.isOver() == true) {
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
			!PlayerData.isParticipating(event.entity.uniqueId)
		) {
			event.foodLevel = 20
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onEntityDeath(event: EntityDeathEvent) {
		val game = UHC.game ?: return

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

			game.playerDeath(uuid, killer, playerData, false)

		} else {
			/* find a quirk that has a dropfix for this entity */
			/* if not fallback to default list of dropfixes */
			(game.quirks.filterNotNull().filter { quirk ->
				quirk.customDrops != null
			}.map { quirk ->
				Util.binaryFind(event.entityType, quirk.customDrops!!) { dropFix -> dropFix.entityType }
			}.firstOrNull()
				?: Util.binaryFind(event.entityType, DropFixType.list) { dropFixType -> dropFixType.dropFix.entityType }?.dropFix
			)?.onDeath(event.entity, killer, event.drops)

			game.quirks.filterNotNull().any { quirk ->
				quirk.modifyEntityDrops(event.entity, killer, event.drops)
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

		/* stuff that happens during the game */
		if (playerData.participating) {
			if (UHC.game?.quirkEnabled(QuirkType.LOW_GRAVITY) == true && event.cause == EntityDamageEvent.DamageCause.FALL) {
				event.isCancelled = true

			} else if (event.entity is Player && UHC.game?.quirkEnabled(QuirkType.DEATHSWAP) == true && Deathswap.untilNextSequence < Deathswap.IMMUNITY) {
				event.isCancelled = true
			}

		/* prevent lobby and postgame damage */
		} else {
			val arena = ArenaManager.playersArena(player.uniqueId)

			if ((arena as? PvpArena)?.isOver() == true || arena == null) event.isCancelled = true
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

		if ((
			attackingPlayer != null &&
			PlayerData.isParticipating(attackingPlayer.uniqueId) &&
			defender is Player &&
			PlayerData.isParticipating(defender.uniqueId)
		) && (
			game.phase is Grace || (
				game.quirkEnabled(QuirkType.PESTS) &&
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

		if (UHC.game?.quirkEnabled(QuirkType.UNSHELTERED) == true && !Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
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
		event.isCancelled = true
		event.block.type = Material.AIR

		val leavesLocation = event.block.location.toCenterLocation()

		val dropPlayer = Bukkit.getOnlinePlayers().filter { PlayerData.isParticipating(it.uniqueId) && it.world === leavesLocation.world }.minByOrNull {
			it.location.distance(leavesLocation)
		}

		/* apply applefix to this leaves block for the nearest player */
		if (dropPlayer != null) BlockFixType.LEAVES.blockFix.onBreakBlock(event.block.state, mutableListOf(), dropPlayer) { drop ->
			if (drop != null) leavesLocation.world.dropItemNaturally(event.block.location, drop)
		}
	}

	@EventHandler
	fun onBucket(event: PlayerBucketEmptyEvent) {
		val game = UHC.game ?: return
		val phase = game.phase as? Endgame ?: return
		val block = event.block

		if (PlayerData.getPlayerData(event.player.uniqueId).participating && block.y > phase.max) {
			phase.addSkybaseBlock(block)
		}
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		val game = UHC.game
		val player = event.player
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* things that affect players playing the game */
		if (game != null && playerData.participating) {
			/* trying to build above endgame top level */
			val phase = game.phase
			if (phase is Endgame && event.blockPlaced.y > phase.max) {
				phase.addSkybaseBlock(event.blockPlaced)
			}

			/* creative block replenishing */
			if (UHC.game?.quirkEnabled(QuirkType.CREATIVE) == true) {
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
			} else if (UHC.game?.quirkEnabled(QuirkType.UNSHELTERED) == true) {
				val block = event.block

				if (!Util.binarySearch(block.type, Unsheltered.acceptedBlocks)) {
					event.isCancelled = true
				}
			}
		} else {
			val pvpGame = ArenaManager.playersArena(player.uniqueId)

			if (pvpGame != null && event.blockPlaced.y > 100) {
				event.player.sendActionBar(Component.text("Height limit for building is 100", NamedTextColor.RED, TextDecoration.BOLD))
				event.isCancelled = true
			}
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
	fun onVehiclePlace(event: VehicleCreateEvent) {
		if (event.vehicle.world === WorldManager.lobbyWorld) event.isCancelled = true
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		val game = UHC.game ?: return

		if (game.quirkEnabled(QuirkType.HOTBAR))
			if (event.clickedInventory?.type == InventoryType.PLAYER && event.slot in 9..35) {
				event.isCancelled = true
			}
		if (game.quirkEnabled(QuirkType.INFINITE_INVENTORY) && event.clickedInventory?.type == InventoryType.PLAYER && event.action != InventoryAction.NOTHING) {
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

		} else {
			val halloween = UHC.game?.getQuirk<Halloween>(QuirkType.HALLOWEEN) ?: return

			if (
				event.entityType === EntityType.PLAYER &&
				event.item.itemStack.type === Material.DIAMOND &&
				event.entity.name != "balduvian"
			) {
				if (!halloween.hasGottenDiamonds) {
					Halloween.jumpScare(event.entity as Player)
					halloween.hasGottenDiamonds = true
				}
			}
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
			event.player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 2400, 2, false, true, true))
		}
	}

	@EventHandler
	fun onGrow(event: BlockGrowEvent) {
		if (
			event.newState.type === Material.SUGAR_CANE &&
			WorldManager.isGameWorld(event.newState.world) &&
			UHC.getConfig().worldGenEnabled(WorldGenOption.SUGAR_CANE_REGEN)
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
}
