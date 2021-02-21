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

	var GLOWING_TIME = 15
	var glowingTimer = 0

	var finished = false

	override fun customStart() {
		EndgameNone.closeNether()

		val world = uhc.getDefaultWorld()

		val (min, max) = AbstractEndgame.determineMinMax(world, uhc.endRadius, 100)
		finalMin = min
		finalMax = max

		this.max = 255
		this.min = finalMin - (this.max - finalMax)
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

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		val world = uhc.getDefaultWorld()
		val extrema = uhc.endRadius

		if (!finished) {
			--max
			++min

			/* teleport players up so they don't fall out the world */
			uhc.allCurrentPlayers { uuid ->
				val location = GameRunner.getPlayerLocation(uuid)

				if (location != null && location.y < min) {
					location.y = min.toDouble()
					GameRunner.teleportPlayer(uuid, location)
				}
			}

			if (min > 0) fillBedrockLayer(world, min - 1)
		}

		/* clear all blocks above the top level */
		for (y in max + 1..255) for (x in -extrema..extrema) for (z in -extrema..extrema) {
			world.getBlockAt(x, y, z).setType(Material.AIR, false)
		}

		if (!finished && max == finalMax) {
			finished = true

			max += 3

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
