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

class EndgameNaturalTerrain : Phase() {
	var min = 0
	var max = 0

	var finalMin = 0
	var finalMax = 0

	val GLOWING_TIME = 15 * 20
	val CLEAR_TIME = 3 * 60 * 20

	var finished = false
	var timer = 0

	override fun customStart() {
		EndgameNone.closeNether()

		val world = uhc.getDefaultWorld()

		val (min, max) = AbstractEndgame.determineMinMax(world, uhc.endRadius, 100)
		finalMin = min
		finalMax = max

		this.max = 255
		this.min = finalMin - (this.max - finalMax)

		timer = 0
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
			(max - finalMax).toDouble() / (255 - finalMax)
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return if (finished)
			"$GOLD${BOLD}Endgame $GOLD${BOLD}${min} - $max"
		else
			"$GOLD${BOLD}Endgame ${RESET}Current: $GOLD${BOLD}${max(min, 0)} - $max ${RESET}Final: $GOLD${BOLD}${finalMin} - $finalMax"
	}

	override fun perTick(currentTick: Int) {
		val world = uhc.getDefaultWorld()
		val extrema = uhc.endRadius
		++timer

		if (finished) {
			if (timer == GLOWING_TIME) {
				timer = 0

				PlayerData.playerDataList.forEach { (uuid, playerData) ->
					/* glow all nonpest players every 15 seconds for 2 seconds */
					if (playerData.alive) GameRunner.potionEffectPlayer(uuid, PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false, true))
				}
			}

		} else {
			val along = timer / CLEAR_TIME.toDouble()

			val newMin = (finalMin * along).toInt()
			val newMax = ((255 - finalMax) * (1 - along) + finalMax).toInt()

			if (newMin != min) {
				min = newMin

				/* teleport players up so they don't fall out the world */
				PlayerData.playerDataList.forEach { (uuid, playerData) ->
					if (playerData.participating) {
						val location = GameRunner.getPlayerLocation(uuid)

						if (location != null && location.y < min) {
							location.y = min.toDouble()
							GameRunner.teleportPlayer(uuid, location)
						}
					}
				}

				/* fill in with bedrock from below */
				if (min > 0) fillBedrockLayer(world, min - 1)
			}

			if (newMax != max) {
				max = newMax

				/* clear all blocks above the top level */
				for (y in max + 1..255) for (x in -extrema..extrema) for (z in -extrema..extrema) {
					world.getBlockAt(x, y, z).setType(Material.AIR, false)
				}
			}

			/* finish */
			if (timer == CLEAR_TIME) {
				timer = 0
				finished = true

				/* add 3 blocks to build above the cleared top */
				max += 3

				/* teleport all sky zombies to the surface */
				PlayerData.playerDataList.forEach { (uuid, playerData) ->
					val zombie = playerData.offlineZombie

					if (zombie != null && zombie.location.y > max - 3) {
						val location = zombie.location
						GameRunner.teleportPlayer(uuid, Location(Bukkit.getWorlds()[0], location.blockX + 0.5, Util.topBlockY(world, location.blockX, location.blockZ).toDouble(), location.blockZ + 0.5))
					}
				}
			}
		}
	}

	override fun perSecond(remainingSeconds: Int) {
		uhc.containSpecs()
	}

	override fun endPhrase() = ""
}
