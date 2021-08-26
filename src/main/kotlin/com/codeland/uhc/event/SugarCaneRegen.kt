package com.codeland.uhc.event

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.phase.phases.Endgame
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

class SugarCaneRegen(val game: Game) {
	companion object {
		const val TICKS_PER_TEAM = 170

		fun length(radius: Int): Int {
			return radius * 2 + 1
		}

		fun size(radius: Int): Int {
			return length(radius) * length(radius)
		}

		fun spaceFromIndex(index: Int, radius: Int): Pair<Int, Int> {
			return Pair((index / length(radius)) - radius, (index % length(radius)) - radius)
		}

		fun isWater(block: Block): Boolean {
			return block.type === Material.WATER || block.type === Material.ICE
		}

		fun findCane(chunk: Chunk): Block? {
			val caneBlock = AbstractChunkPlacer.randomPosition(chunk, 58, 70) { block, _, _, _ ->
				block.type === Material.SUGAR_CANE
			}

			if (caneBlock != null) {
				var max: Block = caneBlock
				while (max.getRelative(BlockFace.UP).type === Material.SUGAR_CANE) max = max.getRelative(BlockFace.UP)
				return max.getRelative(BlockFace.UP)
			}

			return null
		}

		fun placeCane(chunk: Chunk): Boolean {
			val block = findCane(chunk) ?: AbstractChunkPlacer.randomPosition(chunk, 58, 70) { block, _, _, _ ->
				val down = block.getRelative(BlockFace.DOWN)
				down.type === Material.SUGAR_CANE || (
					(
						block.type.isAir || block.type == Material.GRASS
					) && (
					down.type === Material.GRASS_BLOCK ||
						down.type === Material.DIRT        ||
						down.type === Material.SAND        ||
						down.type === Material.PODZOL      ||
						down.type === Material.RED_SAND    ||
						down.type === Material.COARSE_DIRT
					) && (
					isWater(down.getRelative(BlockFace.WEST))  ||
						isWater(down.getRelative(BlockFace.EAST))  ||
						isWater(down.getRelative(BlockFace.NORTH)) ||
						isWater(down.getRelative(BlockFace.SOUTH))
					)
				)
			}

			if (block != null) {
				block.setType(Material.SUGAR_CANE, false)
				println("CANE PLACED AT ${block.x} ${block.y} ${block.z}")
			}

			block?.setType(Material.SUGAR_CANE, false)

			return block != null
		}
	}

	val random = Random(game.world.seed)

	fun reset(world: World): PlaceInfo {
		val radius = ((world.worldBorder.size.coerceAtMost(2000.0) - 1) / 2).coerceAtLeast(1.0).toInt()

		val numValidTeams = TeamData.teams.filter { team -> team.members.any {
			val player = Bukkit.getPlayer(it)
			player?.world === world && player.location.y >= 58
		} }.size

		val timer = ceil(TICKS_PER_TEAM.toFloat() / numValidTeams.coerceAtLeast(1)).toInt()

		val chunkRadius = (radius / 16) - 1

		val placeSpaces = Array(size(chunkRadius)) { it }
		placeSpaces.shuffle(random)

		println("TIMER to $timer with $numValidTeams TEAMS")

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

		if (--info.timer <= 0 && info.chunkRadius >= 0 && (game.phase is Grace || game.phase is Shrink)) {
			val (chunkX, chunkZ) = spaceFromIndex(info.placeSpaces[info.placeIndex], info.chunkRadius)

			if (
				!info.doPlace || (
					placeCane(game.world.getChunkAt(chunkX, chunkZ)) ||
					++info.placeIndex >= size(info.chunkRadius)
				)
			) {
				placeInfo = reset(game.world)
			}
		}
	}
}