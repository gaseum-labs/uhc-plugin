package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor.GOLD
import net.md_5.bungee.api.ChatColor.RESET
import org.bukkit.*
import org.bukkit.ChatColor.BOLD
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.max

class EndgameNaturalTerrain : Endgame() {
	var min = 0
	var max = 0

	var finalMin = 0
	var finalMax = 0

	val GLOWING_TIME = 15 * 20
	val CLEAR_TIME = 3 * 60 * 20

	var finished = false
	var timer = 0

	val MAX_DECAY = 400
	val DECLINE = 4

	class SkybaseBlock(var ticks: Int, val block: Block, val update: Boolean)

	fun skybaseTicks(y: Int): Int {
		val distance = y - finalMax

		return if (distance <= 0) {
			-1
		} else {
			val result = MAX_DECAY - DECLINE * distance
			return if (result < 0) 0 else result
		}
	}

	private val skybaseBlocks = ArrayList<SkybaseBlock>()

	fun addSkybaseBlock(block: Block) {
		skybaseBlocks.add(SkybaseBlock(skybaseTicks(block.y), block, true))
	}

	override fun customStart() {
		super.customStart()

		val world = UHC.getDefaultWorld()

		val (min, max) = determineMinMax(world, UHC.endRadius, 100)
		finalMin = min
		finalMax = max + 10
		if (finalMax > 255) finalMax = 255

		this.max = 255
		this.min = 0

		timer = 0
	}

	fun fillBedrockLayer(world: World, layer: Int) {
		val extrema = UHC.endRadius

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

	fun doUpperLayer(world: World, layer: Int) {
		val extrema = UHC.endRadius

		val ticks = skybaseTicks(layer)

		if (ticks != -1) for (x in -extrema..extrema) {
			for (z in -extrema..extrema) {
				val block = world.getBlockAt(x, layer, z)

				if (ticks == 0)
					block.setType(Material.AIR, false)
				else
					if (!block.type.isAir) skybaseBlocks.add(SkybaseBlock(ticks, block, false))
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
		val world = UHC.getDefaultWorld()
		++timer

		if (finished) {
			if (timer == GLOWING_TIME) {
				timer = 0

				/* glow all nonpest players every 15 seconds for 2 seconds */
				PlayerData.playerDataList.filter { (_, it) -> it.alive }.forEach { (uuid, playerData) ->
					GameRunner.potionEffectPlayer(uuid, PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false, true))
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
				if (max < 255) doUpperLayer(world, max + 1)
			}

			/* finish */
			if (timer == CLEAR_TIME) {
				timer = 0
				finished = true

				/* teleport all sky zombies to the surface */
				PlayerData.playerDataList.mapNotNull { (uuid, playerData) -> playerData.offlineZombie }
					.filter { it.location.y > max }
					.forEach { zombie ->
					val x = zombie.location.blockX
					val z = zombie.location.blockX
					zombie.teleport(Location(UHC.getDefaultWorld(), x + 0.5, Util.topBlockY(world, x, z).toDouble(), z + 0.5))
				}
			}
		}

		val skybasePlayers = Bukkit.getOnlinePlayers()
			.filter { PlayerData.isParticipating(it.uniqueId) && it.location.world === world && it.location.block.y >= finalMax }

		skybaseBlocks.removeIf { skybaseBlock ->
			--skybaseBlock.ticks

			if (skybaseBlock.ticks <= 0 || skybaseBlock.block.type.isAir) {
				skybaseBlock.block.setType(Material.AIR, skybaseBlock.update)

				true
			} else {
				if (skybaseBlock.ticks <= 40 && skybaseBlock.ticks % 10 == 0) {
					val location = 	skybaseBlock.block.location.toCenterLocation()

					skybasePlayers.filter { it.location.distance(location) < 3.0 }.forEach { player ->
						player.spawnParticle(Particle.REDSTONE, location, 16, 0.25, 0.25, 0.25, Particle.DustOptions(particleColor(skybaseBlock.ticks), 1.0f))
					}
				}

				false
			}
		}
	}

	fun particleColor(ticks: Int): Color {
		return when {
			ticks == 40 -> Color.fromRGB(255, 255, 0)
			else -> Color.fromRGB(255, 0, 0)
		}
	}

	override fun perSecond(remainingSeconds: Int) {
		UHC.containSpecs()
	}

	override fun endPhrase() = ""
}
