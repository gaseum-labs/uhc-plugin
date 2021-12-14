package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import org.bukkit.*
import java.lang.StrictMath.abs
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

	fun getPlayerChunks(world: World): ArrayList<ArrayList<Loc>> {
		return group(
			world.players
				.filter { it.location.y >= 58 && PlayerData.isParticipating(it.uniqueId) }
				.map { Loc(it.chunk.x, it.chunk.z) },
			chunkRadius
		)
	}

	var playerChunks: ArrayList<ArrayList<Loc>> = ArrayList()
	var timer = 0
	var tryIndex = 0

	fun tick() {
		val world = game.getOverworld()

		if (--timer <= 0) {
			playerChunks = getPlayerChunks(world)
			timer = getNextTicks(world)
			tryIndex = 0
		}

		playerChunks.removeIf { locs ->
			if (tryIndex < locs.size) {
				val (chunkX, chunkZ) = locs[tryIndex]
				place(world.getChunkAt(chunkX, chunkZ))

			} else {
				true
			}
		}

		++tryIndex
	}

	companion object {
		data class Loc(val x: Int, val z: Int)
		data class Eligible(var group: Int, val loc: Loc)

		class OffsetGrid(val left: Int, val right: Int, val down: Int, val up: Int) {
			val width = right - left + 1
			val height = up - down + 1

			val grid = Array(width * height) { -1 }

			fun get(x: Int, z: Int): Int {
				return grid[(z - down) * width + (x - left)]
			}

			fun set(x: Int, z: Int, value: Int) {
				grid[(z - down) * width + (x - left)] = value
			}
		}

		fun group(players: List<Loc>, radius: Int): ArrayList<ArrayList<Loc>> {
			val eligible = players.mapIndexed { index, loc -> Eligible(index, loc) }

			/* find which locations are overlapping */
			for (i in 0..eligible.lastIndex - 1) {
				for (j in i + 1..eligible.lastIndex) {
					val player0 = eligible[i]
					val player1 = eligible[j]

					if (
						abs(player0.loc.x - player1.loc.x) < radius * 2 + 1 &&
						abs(player0.loc.z - player1.loc.z) < radius * 2 + 1
					) {
						player1.group = player0.group
					}
				}
			}

			/* put the overlapping locs in groups */
			val groups = Array<ArrayList<Loc>>(eligible.size) { ArrayList() }
			eligible.forEach { (group, loc) -> groups[group].add(loc) }

			return groups.flatMap { locs ->
				if (locs.isEmpty()) return@flatMap emptyList<ArrayList<Loc>>()
				/* overlap order chosen randomly */
				locs.shuffle()

				/* determine bounds of the group */
				/* z         */
				/* ^         */
				/* |         */
				/* |         */
				/* +-----> x */
				var left = Int.MAX_VALUE
				var right = Int.MIN_VALUE
				var down = Int.MAX_VALUE
				var up = Int.MIN_VALUE

				locs.forEach { (x, z) ->
					if (x - radius < left) left = x - radius
					if (x + radius > right) right = x + radius
					if (z - radius < down) down = z - radius
					if (z + radius > up) up = z + radius
				}

				/* array of all positions within the bounds of the group */
				val grid = OffsetGrid(left, right, down, up)

				/* assign spaces to the players within, overlapping */
				locs.forEachIndexed { player, (cx, cz) ->
					for (ox in -radius..radius) for (oz in -radius..radius) {
						grid.set(cx + ox, cz + oz, player)
					}
				}

				/* go back over each player's square and see which spaces it retains */
				locs.mapIndexed { player, (cx, cz) ->
					val list = ArrayList<Loc>((radius * 2 + 1) * (radius * 2 + 1))

					for (ox in -radius..radius) for (oz in -radius..radius) {
						if (grid.get(cx + ox, cz + oz) == player) list.add(Loc(cx + ox, cz + oz))
					}

					list.shuffle()
					list
				}
			} as ArrayList<ArrayList<Loc>>
		}
	}
}

//fun main() {
//	val size = 28
//
//	val res = group(arrayListOf(
//		Loc(7, 6),
//		Loc(15, 3),
//		Loc(9, 14),
//		Loc(18, 11),
//		Loc(22, 8),
//		Loc(5, 23),
//		Loc(19, 15),
//		Loc(15, 19),
//	), 2, size - 4)
//
//	val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
//
//	res.forEachIndexed { i, locs ->
//		val shade = (i + 1) * 8
//
//		val pixel = shade.or(shade.shl(8)).or(shade.shl(16)).or(0xff000000.toInt())
//
//		locs.forEach { (x, z) ->
//			if (image.getRGB(x, z) != 0) {
//				println("OVERLAP | $i ($x, $z)")
//			} else if (x >= size || z >= size) {
//				println("OUT OF BOUNDS | $i ($x, $z)")
//			} else {
//				image.setRGB(x, z, pixel)
//			}
//		}
//	}
//
//	ImageIO.write(image, "PNG", File("overlapTest.png"))
//}
