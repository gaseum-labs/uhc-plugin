package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.gui.item.AntiSoftlock
import com.codeland.uhc.util.Util
import com.codeland.uhc.gui.item.GuiOpener
import com.codeland.uhc.gui.item.ParkourCheckpoint
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.quirk.quirks.Pests
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.meta.KnowledgeBookMeta

class WaitingDefault : Phase() {
	var center = 0
	val radius = 30

	override fun customStart() {
		val world = Bukkit.getWorlds()[0]

		center = 10000

		val oceans = arrayOf(
			Biome.OCEAN,
			Biome.DEEP_OCEAN,
			Biome.COLD_OCEAN,
			Biome.DEEP_COLD_OCEAN,
			Biome.FROZEN_OCEAN,
			Biome.DEEP_FROZEN_OCEAN,
			Biome.LUKEWARM_OCEAN,
			Biome.DEEP_LUKEWARM_OCEAN,
			Biome.WARM_OCEAN
		)

		var tries = 0
		while (
			(
				oceans.contains(world.getBiome(center + radius, 60, center + radius)) ||
				oceans.contains(world.getBiome(center + radius, 60, center - radius)) ||
				oceans.contains(world.getBiome(center - radius, 60, center + radius)) ||
				oceans.contains(world.getBiome(center - radius, 60, center - radius))
			) && tries < 1000
		) {
			center += 16
			++tries
		}

		world.setSpawnLocation(center, 70, center)
		world.worldBorder.setCenter(center + 0.5, center + 0.5)
		world.worldBorder.size = radius * 2.0 + 1
		world.isThundering = false
		world.setStorm(false)
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false)
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
		world.time = 6000
		world.difficulty = Difficulty.NORMAL

		Bukkit.getServer().onlinePlayers.forEach { player ->
			player.inventory.clear()
			onPlayerJoin(player)
		}

		uhc.carePackages.onEnd()
	}

	override fun customEnd() {
		Bukkit.getWorlds().forEach { world ->
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
			world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3)
		}
	}

	override fun onTick(currentTick: Int) {
		if (currentTick % 3 == 0)
			Bukkit.getOnlinePlayers().forEach { player ->
				ParkourCheckpoint.updateCheckpoint(player)
			}
	}

	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		barStatic(bossBar)
	}

	override fun endPhrase() = ""

	fun onPlayerJoin(player: Player) {
		player.exp = 0.0F
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.foodLevel = 20
		player.teleport(Location(Bukkit.getWorlds()[0], center + 0.5, Util.topBlockY(Bukkit.getWorlds()[0], center, center) + 1.0, center + 0.5))
		player.gameMode = GameMode.ADVENTURE

		Pests.makeNotPest(player)

		/* get them on the health scoreboard */
		player.damage(0.05)

		val inventory = player.inventory

		if (!GuiOpener.hasItem(inventory)) inventory.addItem(GuiOpener.create())
		if (!AntiSoftlock.hasItem(inventory)) inventory.addItem(AntiSoftlock.create())
		if (!ParkourCheckpoint.hasItem(inventory)) inventory.addItem(ParkourCheckpoint.create())
	}
}
