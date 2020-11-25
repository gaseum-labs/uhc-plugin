package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor.GOLD
import net.md_5.bungee.api.ChatColor.RESET
import org.bukkit.*
import org.bukkit.ChatColor.BOLD
import org.bukkit.ChatColor.WHITE
import org.bukkit.boss.BossBar
import kotlin.math.ceil
import kotlin.math.max

class EndgameNaturalTerrain : Phase() {
	val allowedHeight = 3

	var topBoundary = 0
	var botBoundary = 0
	var center = 0

	var finished = false

	var heightArray = Array((uhc.endRadius * 2 + 1) * (uhc.endRadius * 2 + 1)) { 0 }

	override fun customStart() {
		EndgameNone.closeNether()

		center = Util.randRange(50, 60)

		topBoundary = 255
		botBoundary = center - (255 - center)

		/* fill height array */
		val world = Util.worldFromEnvironment(uhc.defaultEnvironment)

		for (x in -uhc.endRadius..uhc.endRadius) {
			for (z in -uhc.endRadius..uhc.endRadius) {
				val heightArrayIndex = x + uhc.endRadius
			}
		}
	}

	override fun customEnd() {}
	override fun onTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		val world = Util.worldFromEnvironment(uhc.defaultEnvironment)
		val extrema = uhc.endRadius + 6

		if (!finished) {
			--topBoundary
			++botBoundary

			if (topBoundary - botBoundary == 4) {
				/* fill in stone layer so no cave fall throughs */
				for (x in -extrema..extrema)
					for (z in -extrema..extrema) {
						val block = world.getBlockAt(x, center, z)
						if (block.isPassable) block.setType(Material.STONE, false)
					}
			}

			if (topBoundary < center) {
				finished = true

				topBoundary = center + allowedHeight
				botBoundary = center

				/* teleport all zombies to the surface */
				uhc.playerDataList.forEach { (uuid, playerData) ->
					val zombie = playerData.offlineZombie

					if (zombie != null) {
						val location = zombie.location
						GameRunner.teleportPlayer(uuid, Location(Bukkit.getWorlds()[0], location.x, center + 1.0, location.z))
					}
				}
			}
		}

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
