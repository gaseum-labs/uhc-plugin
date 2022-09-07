package org.gaseumlabs.uhc.chc.chcs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.commands.Commands
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
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.chc.CHC
import org.gaseumlabs.uhc.chc.chcs.banana.BananaType
import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.phases.Grace
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.ScoreboardDisplay
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import kotlin.random.Random

fun findGroundAt(x: Int, z: Int, limitedRegion: LimitedRegion): Int? {
	for (y in 128 downTo 58) {
		val block = limitedRegion.getBlockData(x, y, z)
		if (block.material.isSolid && !RegenUtil.treeParts.contains(block.material)) {
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
val leavesData = Material.JUNGLE_LEAVES.createBlockData()
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

		for (i in 0 until height) {
			limitedRegion.setBlockData(cx, startY + i, cz, trunkBlockData)
		}
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

class BananaData(var count: Int, var lastUsed: Int)

class Banana : CHC<BananaData>() {
	private var tickingTask = -1
	private var scoreboard: ScoreboardDisplay? = null
	private var currentSecond = 0

	private val recipes = BananaType.values()
		.take(BananaType.values().size - 1)
		.map { BananaType.genRecipe(it) }

	init {
		recipes.forEach { Bukkit.addRecipe(it) }
	}

	override fun defaultData() = BananaData(0, -12890)

	override fun customDestroy(game: Game) {
		Bukkit.getScheduler().cancelTask(tickingTask)
		scoreboard?.destroy()
		recipes.forEach { Bukkit.removeRecipe(it.key) }
	}

	override fun onPhaseSwitch(game: Game, phase: Phase) {
		if (phase is Grace) {
			scoreboard = ScoreboardDisplay(BananaType.REGULAR.text("Bananas"), 12)
			scoreboard?.show()
			tickingTask = SchedulerUtil.everyN(20) { second(game) }
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

		if (game.phase !is Grace && currentSecond % 60 == 0) {
			val min = playerDatas.last().getQuirkData(this).count
			val smittenPlayers = playerDatas.filter { it.getQuirkData(this).count == min }

			smittenPlayers.forEach { Action.damagePlayer(it, 2.0) }

			val smiteMessages = listOf(BananaType.REGULAR.text("Banana Gods are Angry")) +
				smittenPlayers.map {
					Component.text("Smited ").append(
						BananaType.values().random().text(Bukkit.getOfflinePlayer(it.uuid).name ?: "[unknown]")
					)
				}

			playerDatas.map { Bukkit.getPlayer(it.uuid)?.let { player ->
				smiteMessages.forEach { text -> player.sendMessage(text) }
			}}
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
						.append(BananaType.REGULAR.text(playerData.getQuirkData(this).count.toString()))
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
				BananaType.REGULAR.create()
			)
		}

		@EventHandler
		fun onBlockDrop(event: BlockDropItemEvent) {
			if (event.block.type === Material.SPONGE) {
				event.isCancelled = true
			}
		}

		@EventHandler
		fun entityDeath(event: EntityDeathEvent) {
			if (isMonkey(event.entity)) {
				event.drops.clear()
				event.drops.add(BananaType.REGULAR.create())
			}
		}

		@EventHandler
		fun spawnEntity(event: EntitySpawnEvent) {
			if (event.entity !is Player && event.entity is LivingEntity && Random.nextInt(20) == 0) {
				spawnMonkey(event.entity.location)
				event.isCancelled = true
			}
		}
	}

	companion object {
		const val KEY_MOB = "_uhc_b-mob"
		fun isMonkey(entity: Entity) = entity.getMetadata(KEY_MOB).isNotEmpty()
	}
}