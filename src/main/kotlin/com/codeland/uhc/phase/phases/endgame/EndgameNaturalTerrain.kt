package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor.GOLD
import net.md_5.bungee.api.ChatColor.RESET
import org.bukkit.Bukkit
import org.bukkit.ChatColor.BOLD
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.TileState
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.max
import kotlin.math.round

class EndgameNaturalTerrain : Phase() {
	val SEARCH_MIN = 60
	val SEARCH_MAX = 100

	var topBoundary = 0
	var botBoundary = 0

	var area_min = 0
	var area_max = 0

	var GLOWING_TIME = 15
	var glowingTimer = 0

	var finished = false

	override fun customStart() {
		EndgameNone.closeNether()

		val heightList = ArrayList<Int>((uhc.endRadius * 2 + 1) * (uhc.endRadius * 2 + 1))

		/* find heights of all positions within endgame */
		val world = uhc.getDefaultWorld()

		for (x in -uhc.endRadius..uhc.endRadius) {
			for (z in -uhc.endRadius..uhc.endRadius) {
				var solidCount = 0
				var topLevel = 0
				var foundLevel = false

				for (y in SEARCH_MAX downTo 0) {
					val solid = !world.getBlockAt(x, y, z).isPassable

					if (solidCount == 0) {
						if (solid) {
							topLevel = y
							solidCount = 1
						}
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

				if (foundLevel) heightList.add(topLevel)
			}
		}

		heightList.sort()

		/* not enough data for a good zone */
		if (heightList.size < 32) {
			area_min = 58
			area_max = 66
		} else {
			area_min = heightList[round(heightList.size * 0.10).toInt().coerceAtMost(heightList.lastIndex)]
			area_max = heightList[round(heightList.size * 0.80).toInt().coerceAtMost(heightList.lastIndex)]

			val distance = area_max - area_min + 1

			if (distance < 9) {
				val addedDistance = 9 - distance
				val topAdded = addedDistance / 2
				val bottomAdded = addedDistance - topAdded

				area_max += topAdded
				area_min -= bottomAdded
			}
		}

		topBoundary = 255
		botBoundary = area_min - (topBoundary - area_max)
	}

	fun fillBedrockLayer(world: World, layer: Int) {
		val extrema = uhc.endRadius

		for (x in -extrema..extrema) {
			for (z in -extrema..extrema) {
				val block = world.getBlockAt(x, layer, z)
				val state = block.getState(false)

				if (state is TileState) {
					block.breakNaturally()
				} else {
					block.setType(Material.BEDROCK, false)
				}
			}
		}
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return if (finished)
			1.0
		else
			(topBoundary - area_max).toDouble() / (255 - area_max)
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return if (finished)
			"$GOLD${BOLD}Endgame $GOLD${BOLD}${botBoundary} - $topBoundary"
		else
			"$GOLD${BOLD}Endgame ${RESET}Current: $GOLD${BOLD}${max(botBoundary, 0)} - $topBoundary ${RESET}Final: $GOLD${BOLD}${area_min} - $area_max"
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		val world = uhc.getDefaultWorld()
		val extrema = uhc.endRadius

		if (!finished) {
			--topBoundary
			++botBoundary

			/* teleport players up so they don't fall out the world */
			uhc.allCurrentPlayers { uuid ->
				val location = GameRunner.getPlayerLocation(uuid)

				if (location != null && location.y < botBoundary) {
					location.y = botBoundary.toDouble()
					GameRunner.teleportPlayer(uuid, location)
				}
			}

			if (botBoundary > 0) fillBedrockLayer(world, botBoundary - 1)
		}

		for (y in 0..255)
			if (y < botBoundary - 1 || y > topBoundary)
				for (x in -extrema..extrema)
					for (z in -extrema..extrema)
						world.getBlockAt(x, y, z).setType(Material.AIR, false)


		if (!finished && topBoundary == area_max) {
			finished = true

			topBoundary += 3

			/* teleport all zombies to the surface */
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				val zombie = playerData.offlineZombie

				if (zombie != null) {
					val location = zombie.location
					GameRunner.teleportPlayer(uuid, Location(Bukkit.getWorlds()[0], location.x, Util.topBlockY(world, location.blockX, location.blockZ).toDouble(), location.z))
				}
			}
		}

		if (finished) {
			++glowingTimer
			if (glowingTimer == GLOWING_TIME) {
				glowingTimer = 0

				uhc.allCurrentPlayers { uuid ->
					GameRunner.potionEffectPlayer(uuid, PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false, true))
				}
			}
		}

		uhc.containSpecs()
	}

	override fun endPhrase() = ""
}
