package org.gaseumlabs.uhc.chc.chcs.banana

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.block.data.Orientable
import org.bukkit.entity.Cow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.chc.CHC
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.phases.Grace
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.ScoreboardDisplay
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import kotlin.random.Random
import org.bukkit.event.block.Action as BAction
import org.gaseumlabs.uhc.command.Commands
import java.util.*
import kotlin.collections.ArrayList
import org.bukkit.Sound
import org.bukkit.block.data.type.Leaves

fun findGroundAt(x: Int, z: Int, limitedRegion: LimitedRegion): Int? {
	for (y in 128 downTo 58) {
		val block = limitedRegion.getBlockData(x, y, z)
		if (block.material.isSolid && !RegenUtil.surfaceIgnore.contains(block.material)) {
			return y + 1
		}
	}
	return null
}

fun spawnMonkey(location: Location) {
	val monkey = location.world.spawn(location, Cow::class.java)

	monkey.isCustomNameVisible = true
	monkey.customName(Component.text("UHC Monkey", TextColor.color(0xffff00), TextDecoration.BOLD))
	monkey.activePotionEffects.add(PotionEffect(PotionEffectType.JUMP, 100000, 3))
	monkey.activePotionEffects.add(PotionEffect(PotionEffectType.SPEED, 100000, 3))
	monkey.setMetadata(Banana.KEY_MOB, FixedMetadataValue(UHCPlugin.plugin, true))
	monkey.removeWhenFarAway = true
}

val trunkBlockData = Material.STRIPPED_JUNGLE_LOG.createBlockData().let { data ->
	(data as Orientable).axis = Axis.Y
	data
}
val topTrunkBlockData = Material.JUNGLE_LOG.createBlockData().let { data ->
	(data as Orientable).axis = Axis.Y
	data
}
val leavesData = Material.JUNGLE_LEAVES.createBlockData().let { data ->
	(data as Leaves).distance = 1
	data
}
val spongeBlocKData = Material.SPONGE.createBlockData()

class BananaTree : BlockPopulator() {
	override fun populate(
		worldInfo: WorldInfo,
		random: java.util.Random,
		chunkX: Int,
		chunkZ: Int,
		limitedRegion: LimitedRegion
	) {
		if (random.nextInt(10) != 0) return

		val rx = limitedRegion.centerBlockX
		val rz = limitedRegion.centerBlockZ

		val cx = rx + random.nextInt(-8, 9)
		val cz = rz + random.nextInt(-8, 9)

		val startY = findGroundAt(cx, cz, limitedRegion) ?: return
		if (limitedRegion.getBlockData(cx, startY, cz).material.isSolid) return

		val height = random.nextInt(10, 20)

		for (i in 0 until height - 1) {
			limitedRegion.setBlockData(cx, startY + i, cz, trunkBlockData)
		}
		limitedRegion.setBlockData(cx, startY + height - 1, cz, topTrunkBlockData)

		for (i in -5 until 5) {
			if (i == 0) {
				limitedRegion.setBlockData(cx, startY + height, cz, leavesData)
			} else {
				limitedRegion.setBlockData(cx + i, startY + height, cz, leavesData)
				if (random.nextInt(10) == 0)
					limitedRegion.setBlockData(cx + i, startY + height - 1, cz, spongeBlocKData)
				limitedRegion.setBlockData(cx, startY + height, cz + i, leavesData)
				if (random.nextInt(10) == 0)
					limitedRegion.setBlockData(cx, startY + height - 1, cz + i, spongeBlocKData)
			}
		}
	}
}

class BananaData(var count: Int, var lastUsed: IntArray)

class Banana : CHC<BananaData>() {
	private var tickingTask = -1
	private var scoreboard: ScoreboardDisplay? = null
	private var currentSecond = 0

	init {
		recipes.forEach { if (Bukkit.getRecipe(it.key) == null) Bukkit.addRecipe(it) }
	}

	override fun defaultData() = BananaData(0, IntArray(bananaRegistry.size) { -10000 })

	override fun customDestroy(game: Game) {
		Bukkit.getScheduler().cancelTask(tickingTask)
		scoreboard?.destroy()
		recipes.forEach { Bukkit.removeRecipe(it.key) }
	}

	override fun onPhaseSwitch(game: Game, phase: Phase) {
		if (phase is Grace) {
			scoreboard = ScoreboardDisplay(REGULAR.text("Bananas"), 12)
			scoreboard?.show()
			var tick = 0;
			tickingTask = SchedulerUtil.everyTick {
				if (tick++ % 20 == 0) second(game)
				BananaManager.tick()
			}
		}
	}

	override fun onStartPlayer(game: Game, uuid: UUID) {
		Action.playerAction(uuid) { player ->
			player.discoverRecipes(recipes.map { it.key })
		}
	}

	private fun second(game: Game) {
		++currentSecond

		val playerDatas = ArrayList(PlayerData.playerDataList.map { (_, data) -> data }.filter { it.participating })
		if (playerDatas.isEmpty()) return

		playerDatas.forEach { playerData ->
			val inventory = Action.playerInventory(playerData.uuid)
			val count = inventory?.fold(0) { count, stack ->
				count + (BananaType.getBananaType(stack)?.points ?: 0)
			} ?: 0
			playerData.setQuirkDataL(this) { it.count = count }
		}
		playerDatas.sortBy { it.getQuirkData(this).count }

		updateScoreboard(scoreboard!!, playerDatas)

		if (game.phase !is Grace) {
			val max = playerDatas.first().getQuirkData(this).count
			val topPlayers = playerDatas.filter { it.getQuirkData(this).count == max }

			topPlayers.forEach { Action.potionEffectPlayer(it.uuid, PotionEffect(PotionEffectType.GLOWING, 30, 1)) }

			if (currentSecond % 60 == 0) {
				val min = playerDatas.last().getQuirkData(this).count
				val smittenPlayers = playerDatas.filter { it.getQuirkData(this).count == min }

				smittenPlayers.forEach { Action.damagePlayer(it, 2.0) }

				val smiteMessages = listOf(REGULAR.text("Banana Gods are Angry")) +
					smittenPlayers.map {
						Component.text("Smited ").append(
							bananaRegistry.random().text(Bukkit.getOfflinePlayer(it.uuid).name ?: "[unknown]")
						)
					}

				playerDatas.map { Bukkit.getPlayer(it.uuid)?.let { player ->
					smiteMessages.forEach { text -> player.sendMessage(text) }
				}}
			}
		}
	}

	fun updateScoreboard(
		scoreboard: ScoreboardDisplay,
		playerDatas: List<PlayerData>
	) {
		playerDatas.take(12)
			.forEachIndexed { i, playerData ->
				scoreboard.setLine(i,
					Component.text(
						"${Bukkit.getOfflinePlayer(playerData.uuid).name}"
					).append(Component.text(" - ", NamedTextColor.GRAY))
						.append(REGULAR.text(playerData.getQuirkData(this).count.toString()))
				)
			}
	}

	override fun eventListener() = object : Listener {
		@EventHandler
		fun onWorldLoad(event: WorldInitEvent) {
			event.world.populators.add(BananaTree())
		}

		@EventHandler
		fun onStartBreakBlock(event: BlockDamageEvent) {
			if (event.block.type !== Material.SPONGE) return
			event.instaBreak = true
			event.block.world.dropItemNaturally(
				event.block.location,
				REGULAR.create()
			)
		}

		@EventHandler
		fun onBlockDrop(event: BlockDropItemEvent) {
			if (event.block.type === Material.SPONGE) {
				event.items.clear()
			}
		}

		@EventHandler
		fun entityDeath(event: EntityDeathEvent) {
			if (isMonkey(event.entity)) {
				event.drops.clear()
				event.drops.add(REGULAR.create())
			}
		}

		@EventHandler
		fun spawnEntity(event: EntitySpawnEvent) {
			if (
				event.entity !is Player &&
				event.entity is LivingEntity &&
				Random.nextInt(20) == 0
			) {
				spawnMonkey(event.entity.location)
				event.isCancelled = true
			}
		}

		@EventHandler
		fun onUseItem(event: PlayerInteractEvent) {
			if (
				event.action === BAction.RIGHT_CLICK_AIR ||
				event.action === BAction.RIGHT_CLICK_BLOCK
			) {
				val item = event.item ?: return
				val type = BananaType.getBananaType(item) ?: return

				val bananaData = PlayerData.get(event.player).getQuirkData(this@Banana)

				if (currentSecond < bananaData.lastUsed[type.id] + type.cooldown) {
					return Commands.errorMessage(
						event.player,
						"Banana cooldown, try again in ${bananaData.lastUsed[type.id] + type.cooldown - currentSecond} seconds"
					)
				}

				val result = type.ability(event.player) ?: return
				if (result) {
					bananaData.lastUsed[type.id] = currentSecond
					event.player.playSound(event.player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f)
				} else {
					event.player.playSound(event.player.location, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 0.707107f)
				}
			}
		}
	}

	companion object {
		const val KEY_MOB = "_uhc_b-mob"
		fun isMonkey(entity: Entity) = entity.getMetadata(KEY_MOB).isNotEmpty()
	}
}