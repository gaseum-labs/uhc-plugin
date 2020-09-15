package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.phase.Phase
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.boss.BossBar
import kotlin.math.ceil
import kotlin.math.max

class EndgameClearBlocks : Phase() {

	var topBoundary = 255
	var botBoundary = -135
	var finished = false

	override fun customStart() {}

	override fun customEnd() {}

	override fun onTick(currentTick: Int) {
		val world = Bukkit.getWorlds()[0]

		val extrema = ceil(uhc.endRadius).toInt()
		for (y in 0..255) {
			if (y < botBoundary || y > topBoundary) {
				val min = extrema * 2 * currentTick / 20
				val max = extrema * 2 * (currentTick + 1) / 20 + 1

				for (x in ((min - extrema)..(max - extrema))) {
					for (z in (-extrema..extrema)) {
						world.getBlockAt(x, y, z).setType(Material.AIR, false)
					}
				}
			}
		}

		if (currentTick == 0 && !finished) {
			--topBoundary
			++botBoundary

			Bukkit.getServer().onlinePlayers.forEach { player ->
				player.sendActionBar("Block range is between ${phaseType.chatColor}${ChatColor.BOLD}${max(botBoundary, 0)} ${ChatColor.RESET}and ${phaseType.chatColor}${ChatColor.BOLD}$topBoundary")
			}
		}

		if (botBoundary > 60 || topBoundary < 60) {
			finished = true

			topBoundary = 62
			botBoundary = 60
		}
	}

	override fun perSecond(remainingSeconds: Int) {

	}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		barStatic(bossBar)
	}

	override fun endPhrase(): String {
		return ""
	}
}
