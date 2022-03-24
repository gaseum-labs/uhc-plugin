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
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ceil
import kotlin.math.roundToInt

class Endgame(game: Game, val collapseTime: Int) : Phase(PhaseType.ENDGAME, 0, game) {
	var min = 0
	var max = 255

	val GLOWING_TIME = 20 * 20
	val CLEAR_TIME = 5 * 60 * 20

	var finished = false
	var timer = 0

	val MAX_DECAY = 400
	val DECLINE = 4

	var fakeEntityID = Int.MAX_VALUE

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

		game.world.worldBorder.size = (game.config.endgameRadius.get() * 2 + 1).toDouble()

		timer = 0
	}

	class SkybaseBlock(val time: Int, val block: Block, val updateOnDestroy: Boolean, val fakeEntityID: Int) {
		var timer = 0
	}

	fun skybaseTicks(y: Int): Int {
		val distance = y - game.endgameHighY

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

	override fun updateBarLength(remainingTicks: Int): Float {
		return if (finished)
			(timer / GLOWING_TIME.toFloat())
		else
			(max - game.endgameHighY).toFloat() / (255 - game.endgameHighY)
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int): UHCComponent {
		return UHCComponent.text("Endgame", phaseType.color)
			.andSwitch(finished) {
				UHCComponent.text(" - ", U_WHITE)
					.and("$max ", phaseType.color, UHCStyle.BOLD)
					.and("Glowing in ", U_WHITE)
					.and(
						Util.timeString(ceil((GLOWING_TIME - timer) / 20.0).toInt()),
						phaseType.color,
						UHCStyle.BOLD
					)
			}
			.andSwitch(true) {
				UHCComponent.text(" Current ", U_WHITE)
					.and(min.coerceAtLeast(0).toString(), phaseType.color, UHCStyle.BOLD)
					.and(" - ", phaseType.color)
					.and(max.toString(), phaseType.color, UHCStyle.BOLD)
					.and(" Final: ", U_WHITE)
					.and(game.endgameLowY.toString(), phaseType.color, UHCStyle.BOLD)
					.and(" - ", phaseType.color)
					.and(game.endgameHighY.toString(), phaseType.color, UHCStyle.BOLD)
			}
	}

	fun fillLayer(world: World, layer: Int, type: Material) {
		val extrema = game.config.endgameRadius.get()

		for (x in -extrema..extrema) {
			for (z in -extrema..extrema) {
				val block = world.getBlockAt(x, layer, z)
				if (block.getState(false) is TileState) block.breakNaturally()
				block.setType(type, false)
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
			val along = timer / CLEAR_TIME.toFloat()

			val newMin = Util.interp(0.0f, game.endgameLowY.toFloat(), along).toInt()
			val newMax = Util.interp(255.0f, game.endgameHighY.toFloat(), along).toInt()

			if (newMin != min) {
				min = newMin

				/* teleport players up so they don't fall out the world */
				PlayerData.playerDataList.forEach { (uuid, playerData) ->
					if (playerData.participating) {
						val location = Action.getPlayerLocation(uuid)

						if (location != null && location.y < min) {
							location.y = min.toDouble()
							Action.teleportPlayer(uuid, location)
						}
					}
				}

				/* fill in with bedrock from below */
				if (min > 0) fillLayer(game.world, min - 1, Material.BEDROCK)
			}

			if (newMax != max) {
				max = newMax

				/* clear all blocks above the top level */
				if (max < 255) fillLayer(game.world, max + 1, Material.AIR)
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
						zombie.teleport(Location(game.world,
							x + 0.5,
							Util.topBlockY(game.world, x, z).toDouble(),
							z + 0.5))
					}
			}
		}

		skybaseBlocks.removeIf { skybaseBlock ->
			val breakProgress = if (++skybaseBlock.timer >= skybaseBlock.time || skybaseBlock.block.type.isAir) {
				skybaseBlock.block.setType(Material.AIR, skybaseBlock.updateOnDestroy)
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
