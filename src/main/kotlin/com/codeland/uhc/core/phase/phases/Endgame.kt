package com.codeland.uhc.core.phase.phases

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.Game
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor.GOLD
import net.md_5.bungee.api.ChatColor.RESET
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation
import org.bukkit.*
import org.bukkit.ChatColor.BOLD
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ceil
import kotlin.math.roundToInt

class Endgame(game: Game, val collapseTime: Int) : Phase(PhaseType.ENDGAME, 0, game) {
	var min = 0
	var max = 0

	var finalMin = 0
	var finalMax = 0

	val GLOWING_TIME = 20 * 20
	val CLEAR_TIME = 5 * 60 * 20

	var finished = false
	var timer = 0

	val MAX_DECAY = 400
	val DECLINE = 4

	var fakeEntityID = Int.MAX_VALUE

	class SkybaseBlock(var ticks: Int, val block: Block, val update: Boolean, val fakeEntityID: Int)

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
		skybaseBlocks.add(SkybaseBlock(skybaseTicks(block.y), block, true, fakeEntityID--))
	}

	override fun customStart() {
		SchedulerUtil.nextTick {
			PlayerData.playerDataList.forEach { (uuid, _) ->
				val location = GameRunner.getPlayerLocation(uuid)

				if (location != null && location.world !== game.world) {
					GameRunner.playerAction(uuid) { player -> Commands.errorMessage(player, "Failed to return to home dimension!") }
					GameRunner.damagePlayer(uuid, 100000000000.0)
				}
			}
		}

		game.world.worldBorder.size = (game.config.endgameRadius.get() * 2 + 1).toDouble()

		val (min, max) = determineMinMax(game.world, game.config.endgameRadius.get(), 100)
		finalMin = min
		finalMax = max
		if (finalMax > 255) finalMax = 255

		this.max = 255
		this.min = 0

		timer = 0
	}

	override fun updateBarLength(remainingTicks: Int): Float {
		return if (finished)
			(timer / GLOWING_TIME.toFloat())
		else
			(max - finalMax).toFloat() / (255 - finalMax)
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int): String {
		return if (finished) {
			"$GOLD${BOLD}Endgame $GOLD${BOLD}${min} - $max ${RESET}Glowing in $GOLD${BOLD}${Util.timeString(ceil((GLOWING_TIME - timer) / 20.0).toInt())}"
		} else {
			"$GOLD${BOLD}Endgame ${RESET}Current: $GOLD${BOLD}${min.coerceAtLeast(0)} - $max ${RESET}Final: $GOLD${BOLD}${finalMin} - $finalMax"
		}
	}

	fun fillBedrockLayer(world: World, layer: Int) {
		val extrema = game.config.endgameRadius.get()

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
		val extrema = game.config.endgameRadius.get()

		val ticks = skybaseTicks(layer)

		if (ticks != -1) for (x in -extrema..extrema) {
			for (z in -extrema..extrema) {
				val block = world.getBlockAt(x, layer, z)

				if (ticks == 0)
					block.setType(Material.AIR, false)
				else
					if (!block.type.isAir) skybaseBlocks.add(SkybaseBlock(ticks, block, false, 0))
			}
		}
	}

	override fun perTick(currentTick: Int) {
		++timer

		if (finished) {
			if (timer == GLOWING_TIME) {
				timer = 0

				/* glow all nonpest players every 15 seconds for 2 seconds */
				PlayerData.playerDataList.filter { (_, it) -> it.alive }.forEach { (uuid, _) ->
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
				if (min > 0) fillBedrockLayer(game.world, min - 1)
			}

			if (newMax != max) {
				max = newMax

				/* clear all blocks above the top level */
				if (max < 255) doUpperLayer(game.world, max + 1)
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
					zombie.teleport(Location(game.world, x + 0.5, Util.topBlockY(game.world, x, z).toDouble(), z + 0.5))
				}
			}
		}

		val skybasePlayers = Bukkit.getOnlinePlayers()
			.filter { PlayerData.isParticipating(it.uniqueId) && it.location.world === game.world && it.location.block.y >= finalMax }

		skybaseBlocks.removeIf { skybaseBlock ->
			--skybaseBlock.ticks

			if (skybaseBlock.ticks <= 0 || skybaseBlock.block.type.isAir) {
				skybaseBlock.block.setType(Material.AIR, skybaseBlock.update)

				if (skybaseBlock.update) {
					val packet = PacketPlayOutBlockBreakAnimation(skybaseBlock.fakeEntityID, (skybaseBlock.block as CraftBlock).position, 10)
					skybasePlayers.forEach { (it as CraftPlayer).handle.b.sendPacket(packet) }
				}

				true
			} else {
				if (skybaseBlock.ticks <= 40 && skybaseBlock.update) {
					val packet = PacketPlayOutBlockBreakAnimation(skybaseBlock.fakeEntityID, (skybaseBlock.block as CraftBlock).position, ticksLeftToAnim(skybaseBlock.ticks))
					skybasePlayers.forEach { (it as CraftPlayer).handle.b.sendPacket(packet) }
				}

				false
			}
		}
	}

	private fun ticksLeftToAnim(ticks: Int): Int {
		val along = 40 - ticks

		return if (along < 0) {
			10
		} else {
			((along / 40.0) * 9).roundToInt()
		}
	}

	override fun perSecond(remainingSeconds: Int) {}

	override fun endPhrase() = ""

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
