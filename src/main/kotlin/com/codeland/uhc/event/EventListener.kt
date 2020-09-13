package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.AppleFix
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.NetherFix
import com.codeland.uhc.core.OreFix
import com.codeland.uhc.gui.item.AntiSoftlock
import com.codeland.uhc.util.Util
import com.codeland.uhc.gui.item.GuiOpener
import com.codeland.uhc.gui.item.ParkourCheckpoint
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.grace.GraceDefault
import com.codeland.uhc.phases.waiting.WaitingDefault
import com.codeland.uhc.quirk.*
import com.codeland.uhc.quirk.quirks.*
import net.md_5.bungee.api.ChatColor
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.world.ChunkPopulateEvent
import org.bukkit.generator.BlockPopulator
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class EventListener : Listener {
	@EventHandler
	fun onPlayerHurt(event: EntityDamageEvent) {
		if (GameRunner.uhc.isGameGoing()) {
			if (GameRunner.uhc.isEnabled(QuirkType.LOW_GRAVITY) && event.cause == EntityDamageEvent.DamageCause.FALL) {
				event.isCancelled = true

			} else if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
				val player = event.entity
				if (player is Player) WetSponge.addSponge(player)
				
			}
		} else {
			event.isCancelled = !GameRunner.uhc.isGameGoing() && event.entityType == EntityType.PLAYER
		}
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
		val stack = event.item ?: return

		val summoner = GameRunner.uhc.getQuirk(QuirkType.SUMMONER) as Summoner
		if (summoner.enabled && summoner.onSummon(event)) event.isCancelled = true

		if (GuiOpener.isItem(stack)) {
			GameRunner.uhc.gui.inventory.open(event.player)
		} else if (AntiSoftlock.isItem(stack)) {
			val world = Bukkit.getWorlds()[0]
			event.player.teleport(Location(world, 10000.5, Util.topBlockY(world, 10000, 10000) + 1.0, 10000.5))
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
		if (!GameRunner.uhc.isGameGoing()) return

		val player = event.entity

		if (GameRunner.uhc.isEnabled(QuirkType.HALF_ZATOICHI)) {
			val killer = player.killer
			if (killer != null) HalfZatoichi.onKill(killer)
		}

		if (GameRunner.uhc.isVariant(PhaseVariant.GRACE_UNFORGIVING))
			event.drops.clear()

		/* normal respawns in grace */
		if (!(GameRunner.uhc.isVariant(PhaseVariant.GRACE_FORGIVING) || GameRunner.uhc.isVariant(PhaseVariant.GRACE_UNFORGIVING))) {
			val wasPest = Pests.isPest(player)

			if (GameRunner.uhc.isEnabled(QuirkType.PESTS)) {
				if (!wasPest && event.entity.gameMode != GameMode.SPECTATOR)
					Pests.makePest(player)
			} else {
				player.gameMode = GameMode.SPECTATOR
			}

			if (!wasPest) GameRunner.playerDeath(player, Pests.isPest(player))
		}

		if (GameRunner.uhc.isEnabled(QuirkType.HOTBAR)) {
			event.drops.removeAll { itemStack ->
				itemStack.type == Material.BLACK_STAINED_GLASS_PANE
						&& itemStack.itemMeta.displayName == "Unusable Slot"
			}
		}
	}

	@EventHandler
	fun onMessage(event: AsyncPlayerChatEvent) {
		/* only modify chat behavior with players on teams */
		val team = GameRunner.playersTeam(event.player.name) ?: return

		/* only modify chat when game is running */
		if (GameRunner.uhc.isPhase(PhaseType.WAITING) || GameRunner.uhc.isPhase(PhaseType.POSTGAME)) return

		if (event.message.startsWith("!")) {
			/* prevent blank global messages */
			if (event.message.length == 1)
				event.isCancelled = true
			else
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
	fun onWeather(event: WeatherChangeEvent) {
		event.isCancelled = GameRunner.uhc.isPhase(PhaseType.WAITING) && event.toWeatherState()
	}

	@EventHandler
	fun onPlayerPortal(event: PlayerPortalEvent) {
		if (
			!GameRunner.netherIsAllowed() &&
			event.player.gameMode == GameMode.SURVIVAL
		) {
			val location = event.player.location
			val world = location.world

			/* break the portal */
			world.getBlockAt(location).type = Material.AIR
			Commands.errorMessage(event.player, "Nether is closed!")

			event.isCancelled = true
		}
	}

	@EventHandler
	fun onChunkLoad(event: ChunkPopulateEvent) {
		val world = event.world
		val chunk = event.chunk

		/* prevent animals when the chunks load for waiting area */
		if (GameRunner.uhc.isPhase(PhaseType.WAITING) && world.environment == World.Environment.NORMAL) {
			chunk.entities.forEach { entity ->
				entity.remove()
			}
		}

		/* nether wart generation */
		/* also, generate one in every 4 chunks */
		if (world.environment == World.Environment.NETHER && Math.random() < 0.25)
			when (world.getBiome(chunk.x * 16, 64, chunk.z * 16)) {
				Biome.NETHER_WASTES -> {
					NetherFix.placeWart(chunk, 32, 34) { block, under ->
						block.type == Material.AIR && under.type == Material.SOUL_SAND
					}
				}
				Biome.BASALT_DELTAS -> {
					NetherFix.placeWart(chunk, 32, 99) { block, under ->
						block.type == Material.AIR && under.type == Material.MAGMA_BLOCK
					}
				}
				Biome.CRIMSON_FOREST -> {
					NetherFix.placeWart(chunk, 32, 99) { block, _ ->
						block.type == Material.CRIMSON_ROOTS || block.type == Material.CRIMSON_FUNGUS
					}
				}
				Biome.WARPED_FOREST -> {
					NetherFix.placeWart(chunk, 32, 99) { block, _ ->
						block.type == Material.WARPED_ROOTS || block.type == Material.WARPED_FUNGUS
					}
				}
				Biome.SOUL_SAND_VALLEY -> {
					NetherFix.placeWart(chunk, 32, 99) { block, under ->
						block.type == Material.AIR && under.type == Material.SOUL_SAND
					}
				}
				else -> {}
			}

		//if (world.environment == World.Environment.NORMAL) {
		//	OreFix.removeOres(chunk)
		//}
	}

	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
		/* prevent monsters during waiting */
		event.isCancelled = GameRunner.uhc.isPhase(PhaseType.WAITING) &&
			(
				event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL ||
				event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.BEEHIVE
			) &&
			event.entityType.isAlive

		val location = event.location
		val world = event.location.world

		if (world.environment == World.Environment.NETHER) {
			val chance = if (world.getBiome(location.blockX, location.blockY, location.blockZ) == Biome.BASALT_DELTAS) 0.05 else 0.02

			if (event.entity is LivingEntity && Math.random() < chance) {
				event.isCancelled = true
				world.spawnEntity(location, EntityType.BLAZE)
			}
		}
	}

	private fun spreadRespawn(event: PlayerRespawnEvent) {
		val world = Bukkit.getWorlds()[0]
		val location = GraceDefault.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
		if (location != null) event.respawnLocation = location
	}

	@EventHandler
	fun onPlayerRespawn(event: PlayerRespawnEvent) {
		/* grace respawning */
		if (GameRunner.uhc.isVariant(PhaseVariant.GRACE_FORGIVING) || GameRunner.uhc.isVariant(PhaseVariant.GRACE_UNFORGIVING)) {
			spreadRespawn(event)

		/* pest respawning */
		} else {
			if (!GameRunner.uhc.isEnabled(QuirkType.PESTS))
				return

			var player = event.player

			/* player is set to pest on death */
			if (!Pests.isPest(player))
				return

			/* spread player */
			spreadRespawn(event)

			Pests.givePestSetup(player)
		}
	}

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		val player = event.target
		if (player !is Player) return

		if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE)) {
			if (Math.random() < 0.20) WetSponge.addSponge(player)
		}

		val summoner = GameRunner.uhc.getQuirk(QuirkType.SUMMONER) as Summoner
		if (summoner.enabled && summoner.commander.value) {
			val team = GameRunner.playersTeam(player.name)

			if (team != null && Summoner.isCommandedBy(event.entity, team.color))
				event.isCancelled = true
		}

		if (GameRunner.uhc.isEnabled(QuirkType.PESTS)) {
			if (Pests.isPest(player))
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
	fun onHealthRegen(event: EntityRegainHealthEvent) {
		/* no regeneration in UHC */
		var player = event.entity

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

		if (
			GameRunner.uhc.appleFix &&
			AppleFix.isLeaves(blockState.type) &&
			/* don't replace leaf block drops from shears */
			!(drops.size > 0 && AppleFix.isLeaves(drops[0].itemStack.type))
		) {
			drops.clear()

			AppleFix.onBreakLeaves(blockState.type, player) { drop ->
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
				Bukkit.getScheduler().runTaskLater(UHCPlugin.plugin, {
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
		if (!GameRunner.uhc.appleFix)
			return

		/* drop apple for the nearest player */
		Bukkit.getOnlinePlayers().any { player ->
			if (player.location.distance(event.block.location.toCenterLocation()) < 16) {
				AppleFix.onBreakLeaves(event.block.type, player) { drop ->
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

				var oldItemStack = event.itemInHand.clone()

				val inHand: ItemStack = if (event.hand === EquipmentSlot.HAND)
					event.player.inventory.itemInMainHand.clone()
				else
					event.player.inventory.itemInOffHand.clone()

				Bukkit.getScheduler().runTaskLater(UHCPlugin.plugin, {
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

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.HOTBAR))
			if (event.clickedInventory?.type == InventoryType.PLAYER && event.slot in 9..35) {
				event.isCancelled = true
			}
	}
}