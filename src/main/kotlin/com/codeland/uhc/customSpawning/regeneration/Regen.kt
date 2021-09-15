package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.core.phase.phases.Shrink
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import kotlin.math.ceil
import kotlin.random.Random

abstract class Regen(val game: Game, val ticksPerTeam: Int) {
	companion object {
		fun length(radius: Int): Int {
			return radius * 2 + 1
		}

		fun size(radius: Int): Int {
			return length(radius) * length(radius)
		}

		fun spaceFromIndex(index: Int, radius: Int): Pair<Int, Int> {
			return Pair((index / length(radius)) - radius, (index % length(radius)) - radius)
		}
	}

	val random = Random(game.world.seed)

	abstract fun place(chunk: Chunk): Boolean

	fun reset(world: World): PlaceInfo {
		val radius = ((world.worldBorder.size.coerceAtMost(2000.0) - 1) / 2).coerceAtLeast(1.0).toInt()

		val numValidTeams = TeamData.teams.filter { team -> team.members.any {
			val player = Bukkit.getPlayer(it)
			player?.world === world && player.location.y >= 58
		} }.size

		val timer = ceil(ticksPerTeam.toFloat() / numValidTeams.coerceAtLeast(1)).toInt()

		val chunkRadius = (radius / 16) - 1

		val placeSpaces = Array(size(chunkRadius)) { it }
		placeSpaces.shuffle(random)

		return PlaceInfo(
			timer,
			numValidTeams > 0,
			chunkRadius,
			placeSpaces,
			0
		)
	}

	data class PlaceInfo(
		var timer: Int,
		var doPlace: Boolean,
		var chunkRadius: Int,
		var placeSpaces: Array<Int>,
		var placeIndex: Int
	)

	var placeInfo: PlaceInfo? = null

	fun tick() {
		val info = placeInfo ?: run {
			val i = reset(game.world)
			placeInfo = i
			i
		}

		if (--info.timer <= 0 && info.chunkRadius >= 0) {
			val (chunkX, chunkZ) = spaceFromIndex(info.placeSpaces[info.placeIndex], info.chunkRadius)

			if (
				!info.doPlace || (
					place(game.world.getChunkAt(chunkX, chunkZ)) ||
					++info.placeIndex >= size(info.chunkRadius)
				)
			) {
				placeInfo = reset(game.world)
			}
		}
	}
}