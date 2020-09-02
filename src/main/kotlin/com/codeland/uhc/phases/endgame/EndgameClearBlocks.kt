package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import java.lang.Integer.min
import kotlin.math.max

class EndgameClearBlocks : Phase() {

	var topBoundary = 255
	var botBoundary = -135

	override fun customStart() {}

	override fun customEnd() {}

	override fun onTick(currentTick: Int) {
		val world = Bukkit.getWorlds()[0]

		val extrema = world.worldBorder.size.toInt() / 2 + 1
		for (y in 0..255) {
			if (y < botBoundary || y > topBoundary) {
				val min = extrema * 2 * currentTick / 20
				val max = extrema * 2 * (currentTick + 1) / 20 + 1
				for (x in ((min - extrema)..(max - extrema))) {
					for (z in (-extrema..extrema)) {
						world.getBlockAt(x, y, z).type = Material.AIR
					}
				}
			}
		}

		if (currentTick == 0) {
			--topBoundary
			++botBoundary

			Bukkit.getServer().onlinePlayers.forEach { player ->
				player.sendActionBar("Block range is between ${ChatColor.GOLD}${ChatColor.BOLD}${max(botBoundary, 0)} ${ChatColor.RESET}and ${ChatColor.GOLD}${ChatColor.BOLD}$topBoundary")
			}
		}

		if (botBoundary > 60 || topBoundary < 60) {
			botBoundary = 60
			topBoundary = 60
		}
	}

	override fun perSecond(remainingSeconds: Int) {

	}

	override fun getCountdownString(): String {
		return ""
	}

	override fun endPhrase(): String {
		return ""
	}
}
