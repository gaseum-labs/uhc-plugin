package org.gaseumlabs.uhc.core

import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.lobbyPvp.Spiral
import org.gaseumlabs.uhc.team.AbstractTeam
import org.gaseumlabs.uhc.util.Util
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.block.Block
import java.util.concurrent.*
import kotlin.math.*
import kotlin.random.Random

object PlayerSpreader {
	fun findLocation(
		world: World,
		angle: Double,
		angleDeviation: Double,
		spreadRadius: Double,
		findY: (World, Int, Int) -> Int,
	): Location? {
		val minRadius = spreadRadius / 2

		/* initial (i, j) within the 32 * 32 polar coordinate area is random */
		var position = (Math.random() * 32 * 32).toInt()
		for (iterator in 0 until 32 * 32) {
			val i = position % 32
			val j = position / 32

			/* convert to polar coordinates to search for valid spots */
			val iAngle = (angleDeviation * 2) * (i / 32.0) + (angle - angleDeviation)
			val jRadius = (spreadRadius - minRadius) * (j / 32.0) + minRadius

			val squircleRadius = getSquircleRadius(iAngle, jRadius)

			val x = round(cos(iAngle) * squircleRadius).toInt()
			val z = round(sin(iAngle) * squircleRadius).toInt()
			val y = findY(world, x, z)

			/* if the y check is good then use this position */
			if (y != -1) return Location(world, x + 0.5, y.toDouble() + 1, z + 0.5)

			position = (position + 1) % (32 * 32)
		}

		return null
	}

	fun positiveMod(a: Double, b: Double): Double {
		return (a % b + b) % b
	}

	fun getSquareRadius(angle: Double): Double {
		val angle = positiveMod(angle, Math.PI * 2)

		return when {
			angle < (Math.PI / 4) -> 1 / cos(angle)
			angle < (3 * Math.PI / 4) -> 1 / sin(angle)
			angle < (5 * Math.PI / 4) -> -1 / cos(angle)
			angle < (7 * Math.PI / 4) -> -1 / sin(angle)
			else -> 1 / cos(angle)
		}
	}

	fun getSquircleRadius(angle: Double, radius: Double): Double {
		return (getSquareRadius(angle) * radius + radius) / 2
	}

	fun spreadSinglePlayer(world: World, spreadRadius: Double): Location? {
		for (i in 0 until 16) {
			val location = findLocation(
				world,
				Math.random() * 2 * Math.PI,
				Math.PI * 0.9,
				spreadRadius,
				if (world.environment == World.Environment.NETHER) ::findYMid else ::findYTop
			)
			if (location != null) return location
		}

		return null
	}

	fun spawnIn(block: Block): Boolean {
		return block.isPassable && block.type !== LAVA && !SpawnUtil.isWater(block)
	}

	fun isTree(block: Block): Boolean {
		return when (block.type) {
			OAK_LEAVES,
			BIRCH_LEAVES,
			DARK_OAK_LEAVES,
			SPRUCE_LEAVES,
			ACACIA_LEAVES,
			JUNGLE_LEAVES,
			OAK_LOG,
			BIRCH_LOG,
			DARK_OAK_LOG,
			SPRUCE_LOG,
			ACACIA_LOG,
			JUNGLE_LOG,
			-> true
			else -> false
		}
	}

	fun topGround(chunk: Chunk, x: Int, z: Int): Block {
		for (y in 255 downTo 0) {
			val block = chunk.getBlock(x, y, z)
			if (!block.isPassable && !isTree(block)) return block
		}

		return chunk.getBlock(x, 0, z)
	}

	fun findStarts(chunk: Chunk, numStarts: Int): ArrayList<Block>? {
		val list = ArrayList<Block>(numStarts)
		var highest = 0

		/* see the y range we should be searching in */
		for (i in 0 until 4) {
			for (j in 0 until 4) {
				val y = topGround(chunk, i * 4 + 2, j * 4 + 2).y
				if (y > highest) highest = y
			}
		}

		/* all underwater */
		if (highest < 62) return null

		for (x in 0 until 16) {
			for (z in 0 until 16) {
				for (y in 62 until highest) {
					val block = chunk.getBlock(x, y, z)

					if (
						!isTree(block) && !block.isPassable &&
						spawnIn(block.getRelative(0, 1, 0)) &&
						spawnIn(block.getRelative(0, 2, 0))
					) {
						list.add(block)
						if (list.size == numStarts) return list
					}
				}
			}
		}

		return null
	}

	fun spreadPlayers(
		world: World,
		borderRadius: Int,
		teams: List<AbstractTeam>,
	): CompletableFuture<Array<ArrayList<Block>>> {
		val list = Array<ArrayList<Block>>(teams.size) { ArrayList() }
		val points = spreadPoints(Random(world.seed), teams.size, teams.size)

		return CompletableFuture.allOf(
			*teams.indices.map { i ->
				CompletableFuture.runAsync {
					val point = points[i]
					val team = teams[i]

					val chunkX = ((point.x - 0.5f) * (borderRadius * 2 + 1) / 16.0f).toInt()
					val chunkZ = ((point.y - 0.5f) * (borderRadius * 2 + 1) / 16.0f).toInt()

					val spiral = Spiral.defaultSpiral()

					for (t in 0 until 25) {
						val chunkLimit = (borderRadius / 16)
						val x = chunkX + spiral.getX()
						val z = chunkZ + spiral.getZ()

						if (x > chunkLimit || x < -chunkLimit || z > chunkLimit || z < -chunkLimit) {
							spiral.next()
							continue
						}

						val startBlocks = world.getChunkAtAsync(x, z).thenApply { chunk ->
							findStarts(chunk, team.members.size)
						}.get()

						if (startBlocks != null) {
							list[i] = startBlocks
							return@runAsync
						} else {
							spiral.next()
						}
					}

					throw Exception("Could not find chunk to spawn team $i")
				}
			}.toTypedArray()
		).thenApply {
			list
		}
	}

	/**
	 * used for finding a spawn y value at a certain x, z position
	 * in chunks using nether noise generation
	 *
	 * @return a y value to spawn the player at, -1 if no good value
	 * could be found
	 */
	fun findYMid(world: World, x: Int, z: Int): Int {
		val chunk = world.getChunkAt(Location(world, x.toDouble(), 0.0, z.toDouble()))
		val subX = Util.mod(x, 16)
		val subZ = Util.mod(z, 16)

		val low = 30
		val high = 100
		val height = high - low

		val offset = Random(world.seed + x + z).nextInt(0, height + 1)

		for (y in 0..height) {
			val usingY = (y + offset) % height + low

			if (
				chunk.getBlock(subX, usingY + 2, subZ).isEmpty &&
				chunk.getBlock(subX, usingY + 1, subZ).isEmpty &&
				!chunk.getBlock(subX, usingY, subZ).isPassable
			) return usingY
		}

		return -1
	}

	fun findYTop(world: World, x: Int, z: Int): Int {
		val chunk = world.getChunkAt(world.getBlockAt(x, 0, z))
		val ground = topGround(chunk, Util.mod(x, 16), Util.mod(z, 16))

		return if (spawnIn(ground.getRelative(0, 1, 0)) && spawnIn(ground.getRelative(0, 2, 0))) {
			ground.y
		} else {
			-1
		}
	}

	data class Point(var x: Float, var y: Float) {
		fun add(other: Point): Point {
			x += other.x
			y += other.y
			return this
		}

		fun to(otherPoint: Point): Point {
			return Point(otherPoint.x - x, otherPoint.y - y)
		}

		fun shift(ax: Int, ay: Int): Point {
			return Point(x + ax, y + ay)
		}

		fun modPoints(otherPoint: Point): Array<Point> {
			return if (x < 0.5f) {
				if (y < 0.5) {
					arrayOf(otherPoint, otherPoint.shift(-1, 0), otherPoint.shift(-1, -1), otherPoint.shift(0, -1))
				} else {
					arrayOf(otherPoint, otherPoint.shift(-1, 0), otherPoint.shift(-1, 1), otherPoint.shift(0, 1))
				}
			} else {
				if (y < 0.5) {
					arrayOf(otherPoint, otherPoint.shift(1, 0), otherPoint.shift(1, -1), otherPoint.shift(0, -1))
				} else {
					arrayOf(otherPoint, otherPoint.shift(1, 0), otherPoint.shift(1, 1), otherPoint.shift(0, 1))
				}
			}
		}

		fun length(): Float {
			return sqrt((y * y) + (x * x))
		}

		fun mul(scale: Float): Point {
			x *= scale
			y *= scale
			return this
		}

		fun wrap(): Point {
			x = Util.mod(x, 1.0f)
			y = Util.mod(y, 1.0f)
			return this
		}
	}

	fun spreadPoints(random: Random, numPoints: Int, iterations: Int): Array<Point> {
		val points = Array(numPoints) {
			Point(random.nextFloat(), random.nextFloat())
		}
		val vectors = Array(numPoints) { Point(0.0f, 0.0f) }

		val optimalDistance = 1.0f / sqrt(numPoints.toFloat())

		for (i in 0 until iterations) {
			for (p in 0 until numPoints) {
				val point = points[p]

				/* reset vector */
				val vector = Point(0.0f, 0.0f)
				var affectedCount = 0

				/* collect the repelling forces of the points around this point */
				for (k in 0 until numPoints) if (k != p) {
					point.modPoints(points[k]).forEach { otherPoint ->
						val toVector = point.to(otherPoint)

						val affection = (1.0f - toVector.length() / optimalDistance).coerceAtLeast(0.0f)

						if (affection > 0.0f) {
							++affectedCount
							vector.add(toVector.mul(affection))
						}
					}
				}

				/* average and flip */
				vectors[p] = if (affectedCount == 0) {
					Point(0.0f, 0.0f)
				} else {
					vector.mul(-1.0f / affectedCount)
				}
			}

			for (p in 0 until numPoints) {
				points[p].add(vectors[p])
				points[p].wrap()
			}
		}

		return points
	}
}

//fun main() {
//	fun generateImage(size: Int, points: Array<Point>, no: Int) {
//		val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
//
//		points.forEach { (fx, fy) ->
//			val x = (fx * size).roundToInt().coerceIn(0 until size)
//			val y = (fy * size).roundToInt().coerceIn(0 until size)
//
//			image.setRGB(x, y, 0xFFFF0000.toInt())
//		}
//
//		ImageIO.write(image, "PNG", File("testSpread${no}.png"))
//	}
//
//	generateImage(50, spreadPoints(Random(Random.nextLong()), 8, 16), 0)
//}
