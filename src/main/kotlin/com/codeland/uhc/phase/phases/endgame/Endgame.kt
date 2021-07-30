
package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.World
import kotlin.math.roundToInt

abstract class Endgame : Phase() {
	override fun customStart() {
		SchedulerUtil.nextTick {
			val defaultWorld = UHC.getDefaultWorldGame()

			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				val location = GameRunner.getPlayerLocation(uuid)

				if (location != null && location.world !== defaultWorld) {
					GameRunner.playerAction(uuid) { player -> Commands.errorMessage(player, "Failed to return to home dimension!") }
					GameRunner.damagePlayer(uuid, 100000000000.0)
				}
			}
		}

		val world = UHC.getDefaultWorldGame()
		world.worldBorder.size = (UHC.endRadius() * 2 + 1).toDouble()
	}

	companion object {
		val RANGE = 24

		fun determineMinMax(world: World, radius: Int, maxHeight: Int): Pair<Int, Int> {
			/* store every recorded height of every x z coordinate within the radius */
			val heightList = ArrayList<Int>((radius * 2 + 1) * (radius * 2 + 1))

			for (x in -radius..radius) {
				for (z in -radius..radius) {
					var solidCount = 0
					var topLevel = 0
					var foundLevel = false

					/* start looking for solid blocks down from maxHeight */
					for (y in maxHeight downTo 0) {
						val solid = !world.getBlockAt(x, y, z).isPassable

						/* the first solid block hit is recorded as the top level */
						if (solidCount == 0) {
							if (solid) {
								topLevel = y
								solidCount = 1
							}
							/* need a chain of 6 solid blocks below top level to count as ground */
						} else {
							if (solid) {
								++solidCount
								if (solidCount == 6) {
									foundLevel = true
									break
								}
							} else {
								solidCount = 0
							}
						}
					}

					/* only record heights if ground was found at this coordinate */
					if (foundLevel) heightList.add(topLevel)
				}
			}

			/* order the height list to find percentiles */
			heightList.sort()

			val median60 = if (heightList.isEmpty()) {
				62
			} else {
				heightList[(heightList.size * 0.60).roundToInt().coerceAtMost(heightList.lastIndex)]
			}

			val below = RANGE / 2
			val above = RANGE - below

			return Pair(median60 - below + 1, median60 + above)
		}
	}
}
