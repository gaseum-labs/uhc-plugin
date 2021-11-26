package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.util.Action
import org.bukkit.Chunk
import org.bukkit.World
import kotlin.math.floor
import kotlin.random.Random

abstract class Regen(val game: Game, val chunkRadius: Int, val ticksPerGenerate: Int) {
	val random = Random(game.world.seed)

	abstract fun place(chunk: Chunk): Boolean

	/**
	 * @return multiplier for how much should spawn based on the world border size
	 * from 1.0 to 0.0
	 *
	 * will be 1.0 when world border is above the chunk radius
	 */
	fun regenScale(world: World): Float {
		val borderDiameter = world.worldBorder.size.toFloat()
		val regenDiameter = (chunkRadius * 2 + 1) * 16.0f

		return (borderDiameter / regenDiameter).coerceAtMost(1.0f)
	}

	fun getNextTicks(world: World): Int {
		return if (regenScale(world) == 0.0f) {
			Int.MAX_VALUE
		} else {
			(ticksPerGenerate / regenScale(world)).coerceAtLeast(1.0f).toInt()
		}
	}

	fun getTeamChunks(world: World): ArrayList<List<Array<Pair<Int, Int>>>> {
		val borderChunk = ((world.worldBorder.size / 2).toInt() / 16) - 1

		/* only spawn for one eligible teammate per team chosen at random */
		return game.teams.teams().map { team ->
			team.members.filter {
				PlayerData.isParticipating(it)
			}.mapNotNull {
				Action.getPlayerLocation(it)
			}.filter {
				it.world === world && it.y >= 58
			}.shuffled()
		}.filter {
			it.isNotEmpty()
		}.map { locations ->
			locations.map { location ->
				val cx = floor(location.blockX / 16.0).toInt()
				val cz = floor(location.blockZ / 16.0).toInt()

				val left = (cx - chunkRadius).coerceAtLeast(-borderChunk)
				val right = (cx + chunkRadius).coerceAtMost(borderChunk)
				val up = (cz - chunkRadius).coerceAtLeast(-borderChunk)
				val down = (cz + chunkRadius).coerceAtMost(borderChunk)

				val width = (right - left + 1)
				val height = (down - up + 1)

				val list = Array(width * height) { i ->
					Pair(left + (i % width), up + (i / width))
				}

				list.shuffle()

				list
			}
		} as ArrayList<List<Array<Pair<Int, Int>>>>
	}

	var teamChunks: ArrayList<List<Array<Pair<Int, Int>>>> = ArrayList()
	var timer = 0
	var tryIndex = 0

	fun tick() {
		val world = game.getOverworld()

		if (--timer <= 0) {
			teamChunks = getTeamChunks(world)
			timer = getNextTicks(world)
			tryIndex = 0
		}

		teamChunks.removeIf { players ->
			players.any { chunks ->
				if (tryIndex < chunks.size) {
					val (chunkX, chunkZ) = chunks[tryIndex]
					place(world.getChunkAt(chunkX, chunkZ))

				} else {
					true
				}
			}
		}

		++tryIndex
	}
}