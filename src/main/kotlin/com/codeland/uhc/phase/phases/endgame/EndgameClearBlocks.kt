package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor.GOLD
import net.md_5.bungee.api.ChatColor.RESET
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.BOLD
import org.bukkit.ChatColor.WHITE
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.boss.BossBar
import kotlin.math.ceil
import kotlin.math.max

class EndgameClearBlocks : Phase() {
	val allowedHeight = 3

	var topBoundary = 0
	var botBoundary = 0
	var center = 0

	var finished = false

	override fun customStart() {
		EndgameNone.closeNether()

		center = Util.randRange(50, 60)

		topBoundary = 255
		botBoundary = center - (255 - center)
	}

	override fun customEnd() {}
	override fun onTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		val world = Bukkit.getWorlds()[0]

		if (!finished) {
			--topBoundary
			++botBoundary

			if (botBoundary > center || topBoundary < center) {
				finished = true

				topBoundary = center + allowedHeight
				botBoundary = center
			}
		}

		val extrema = ceil(uhc.endRadius).toInt()

		for (y in 0..255)
			if (y < botBoundary || y > topBoundary)
				for (x in -extrema..extrema)
					for (z in -extrema..extrema)
						world.getBlockAt(x, y, z).setType(Material.AIR, false)
	}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		bossBar.setTitle("$GOLD${BOLD}Endgame ${RESET}Min: $GOLD${BOLD}${max(botBoundary, 0)} ${RESET}Center: $GOLD${BOLD}${center} ${RESET}Max: $GOLD${BOLD}${max(topBoundary, center + allowedHeight)}")
		bossBar.progress = (topBoundary - center) / (255.0 - center)
	}

	override fun endPhrase() = ""
}

//val extrema = ceil(uhc.endRadius).toInt()
//for (y in 0..255) {
//	if (y < botBoundary || y > topBoundary) {
//		val min = extrema * 2 * currentTick / 20
//		val max = extrema * 2 * (currentTick + 1) / 20
//
//		for (x in ((min - extrema)..(max - extrema))) {
//			for (z in (-extrema..extrema)) {
//				world.getBlockAt(x, y, z).setType(Material.AIR, false)
//			}
//		}
//	}
//}
