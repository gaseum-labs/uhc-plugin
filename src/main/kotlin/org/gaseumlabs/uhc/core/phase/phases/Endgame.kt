package org.gaseumlabs.uhc.core.phase.phases

import org.gaseumlabs.uhc.component.*
import org.gaseumlabs.uhc.component.UHCColor.U_WHITE
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.event.Portal
import org.gaseumlabs.uhc.util.*
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.*

class Endgame(game: Game, val radius: Int, private val collapseTime: Int) : Phase(PhaseType.ENDGAME, 0, game) {
	val finalHighLimit: Int
	var highLimit: Int
	var lowLimit: Int

	var finished = false
	var timer = 0

	var fakeEntityID = Int.MAX_VALUE

	val bedrockHeights = IntArray((radius * 2 + 1) * (radius * 2 + 1))
	val MIN_HEIGHT = 61
	val MAX_HEIGHT = 255
	val CHECK_ABOVE = 5

	val treeParts = arrayOf(
		OAK_LEAVES,
		BIRCH_LEAVES,
		ACACIA_LEAVES,
		SPRUCE_LEAVES,
		JUNGLE_LEAVES,
		DARK_OAK_LEAVES,
		AZALEA_LEAVES,
		FLOWERING_AZALEA_LEAVES,
		RED_MUSHROOM_BLOCK,
		BROWN_MUSHROOM_BLOCK,
		MUSHROOM_STEM,
		OAK_LOG,
		BIRCH_LOG,
		ACACIA_LOG,
		SPRUCE_LOG,
		JUNGLE_LOG,
		DARK_OAK_LOG,
		BAMBOO,
		COCOA,
		LILY_PAD,
	)

	private fun air(block: Block): Boolean {
		return block.isPassable || treeParts.contains(block.type)
	}

	private fun topOfColumnFrom(world: World, x: Int, initialY: Int, z: Int): Int {
		for (y in initialY until MAX_HEIGHT) {
			if (air(world.getBlockAt(x, y, z))) {
				return y - 1
			}
		}

		return MAX_HEIGHT
	}

	private fun columnHeight(world: World, x: Int, z: Int): Int {
		var runningTop = MIN_HEIGHT + 1

		while (true) {
			runningTop = topOfColumnFrom(world, x, runningTop, z)
			if (runningTop == MAX_HEIGHT) return MAX_HEIGHT

			var terrainAbove = false

			for (y in runningTop + 2..runningTop + CHECK_ABOVE) {
				if (!air(world.getBlockAt(x, y, z))) {
					runningTop = y
					terrainAbove = true
					break
				}
			}

			if (!terrainAbove) break
		}

		return runningTop - 1
	}

	private fun columnIndex(x: Int, z: Int): Int {
		val width = radius * 2 + 1
		return (z + radius) * width + (x + radius)
	}

	init {
		/* send players in nether back to the overworld */
		SchedulerUtil.nextTick {
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				if (playerData.participating && Action.getPlayerLocation(uuid)?.world !== game.world) {
					val player = Bukkit.getPlayer(uuid)

					if (player != null) {
						Action.sendGameMessage(player, "Returned to Home Dimension")
					}

					if (!Portal.sendThroughPortal(uuid, player)) {
						game.playerDeath(uuid, null, playerData, true)
					}
				}
			}
		}

		game.world.worldBorder.size = (radius * 2 + 1).toDouble()

		for (x in -radius..radius) {
			for (z in -radius..radius) {
				bedrockHeights[columnIndex(x, z)] = columnHeight(game.world, x, z)
			}
		}

		val minHeight = bedrockHeights.minOrNull()!!
		finalHighLimit = minHeight + RANGE

		for (i in bedrockHeights.indices) {
			if (bedrockHeights[i] > finalHighLimit - 1) {
				bedrockHeights[i] = finalHighLimit - 1
			}
		}

		highLimit = WORLD_MAX
		lowLimit = WORLD_MIN

		timer = 0
	}

	class SkybaseBlock(val time: Int, val block: Block, val updateOnDestroy: Boolean, val fakeEntityID: Int) {
		var timer = 0
	}

	private val skybaseBlocks = ArrayList<SkybaseBlock>()

	fun addSkybaseBlock(block: Block) {
		skybaseBlocks.add(SkybaseBlock(SKY_DECAY, block, true, fakeEntityID--))
	}

	override fun updateBarLength(remainingTicks: Int): Float {
		return if (finished)
			timer / GLOWING_TIME.toFloat()
		else
			timer / (collapseTime * 20.0f)
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int): UHCComponent {
		return UHCComponent.text("Endgame", phaseType.color)
			.andSwitch(finished) {
				UHCComponent.text(" Glowing in ", U_WHITE)
					.and(
						Util.timeString(ceil((GLOWING_TIME - timer) / 20.0).toInt()),
						phaseType.color,
						UHCStyle.BOLD
					)
			}
			.andSwitch(true) {
				UHCComponent.text(" High Limit ", U_WHITE)
					.and(highLimit.toString(), phaseType.color, UHCStyle.BOLD)
					.and(" Final ", U_WHITE)
					.and(finalHighLimit.toString(), phaseType.color, UHCStyle.BOLD)
			}
	}

	fun fillLayerAir(world: World, layer: Int) {
		for (x in -radius..radius) {
			for (z in -radius..radius) {
				val block = world.getBlockAt(x, layer, z)
				if (block.getState(false) is TileState) block.breakNaturally()
				block.setType(AIR, false)
			}
		}
	}

	fun fillLayerBedrock(world: World, layer: Int) {
		for (x in -radius..radius) {
			for (z in -radius..radius) {
				if (layer <= bedrockHeights[columnIndex(x, z)]) {
					val block = world.getBlockAt(x, layer, z)
					if (block.getState(false) is TileState) block.breakNaturally()
					block.setType(BEDROCK, false)
				}
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
					Action.potionEffectPlayer(uuid, PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false, true))
				}
			}

		} else {
			val along = timer / (collapseTime * 20.0f)

			val newHighLimit = Util.interp(WORLD_MAX.toFloat() - 1, finalHighLimit.toFloat(), along).toInt()
			val newLowLimit = Util.interp(WORLD_MIN.toFloat() + 1, finalHighLimit.toFloat(), along).toInt()

			if (newLowLimit != lowLimit) {
				lowLimit = newLowLimit

				/* fill in with bedrock from below */
				fillLayerBedrock(game.world, lowLimit - 1)

				/* teleport players up out of bedrock */
				for ((uuid, playerData) in PlayerData.playerDataList) {
					if (playerData.participating) {
						val player = Bukkit.getPlayer(uuid) ?: continue
						if (player.location.block.type === BEDROCK) {
							Action.teleportPlayer(uuid, player.location.add(0.0, 1.0, 0.0))
						}
					}
				}
			}

			if (newHighLimit != highLimit) {
				highLimit = newHighLimit

				/* clear all blocks above the top level */
				fillLayerAir(game.world, highLimit + 1)
			}

			/* finish */
			if (timer >= collapseTime * 20) {
				timer = 0
				finished = true

				/* teleport all sky zombies to the surface */
				PlayerData.playerDataList.mapNotNull { (_, playerData) -> playerData.offlineZombie }
					.filter { it.location.y > highLimit }
					.forEach { zombie ->
						val x = zombie.location.blockX
						val z = zombie.location.blockX
						zombie.teleport(Location(
							game.world,
							x + 0.5,
							Util.topBlockY(game.world, x, z).toDouble(),
							z + 0.5
						))
					}
			}
		}

		skybaseBlocks.removeIf { skybaseBlock ->
			val breakProgress = if (++skybaseBlock.timer >= skybaseBlock.time || skybaseBlock.block.type.isAir) {
				skybaseBlock.block.setType(AIR, skybaseBlock.updateOnDestroy)
				10
			} else {
				ticksLeftToAnim(skybaseBlock)
			}

			if (skybaseBlock.updateOnDestroy) {
				val packet = ClientboundBlockDestructionPacket(
					skybaseBlock.fakeEntityID,
					(skybaseBlock.block as CraftBlock).position,
					breakProgress
				)

				skybaseBlock.block.world.players.forEach {
					(it as CraftPlayer).handle.connection.send(packet)
				}
			}

			breakProgress == 10
		}
	}

	private fun ticksLeftToAnim(skybaseBlock: SkybaseBlock): Int {
		return Util.interp(0.0f, 9.0f, skybaseBlock.timer / skybaseBlock.time.toFloat()).roundToInt()
	}

	override fun perSecond(remainingSeconds: Int) {}

	override fun endPhrase() = ""

	companion object {
		const val WORLD_MIN = -64
		const val WORLD_MAX = 319

		const val GLOWING_TIME = 20 * 20

		const val SKY_DECAY = 300
		const val DECLINE = 4

		const val BUILD_ADD = 9

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
