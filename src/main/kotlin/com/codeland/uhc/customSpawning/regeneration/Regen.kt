package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Action
import org.bukkit.Chunk
import org.bukkit.World
import kotlin.math.floor
import kotlin.random.Random

abstract class Regen(val game: Game, val chunkRadius: Int, val ticksPerGenerate: Int) {
	val random = Random(game.world.seed)

	abstract fun place(chunk: Chunk): Boolean

	fun getPlayerChunks(world: World): ArrayList<Array<Pair<Int, Int>>> {
		val borderChunk = ((world.worldBorder.size / 2).toInt() / 16) - 1

		/* only spawn for one eligible teammate per team chosen at random */
		return TeamData.teams.mapNotNull { team ->
			team.members.filter {
				PlayerData.isParticipating(it)
			}.mapNotNull {
				Action.getPlayerLocation(it)
			}.filter {
				it.world === world && it.y >= 58
			}.randomOrNull()
		}.map { location ->
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
		} as ArrayList<Array<Pair<Int, Int>>>
	}

	var playerChunks: ArrayList<Array<Pair<Int, Int>>> = ArrayList()
	var timer = 0
	var tryIndex = 0

	fun tick() {
		if (--timer <= 0) {
			playerChunks = getPlayerChunks(game.getOverworld())
			timer = ticksPerGenerate
			tryIndex = 0
		}

		playerChunks.removeIf { chunkList ->
			if (tryIndex < chunkList.size) {
				val (chunkX, chunkZ) = chunkList[tryIndex]
				val chunk = game.world.getChunkAt(chunkX, chunkZ)

				place(chunk)

			} else {
				true
			}
		}

		++tryIndex
	}
}