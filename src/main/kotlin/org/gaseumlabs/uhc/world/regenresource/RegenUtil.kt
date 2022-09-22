package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.UP
import org.gaseumlabs.uhc.util.IntVector
import org.gaseumlabs.uhc.util.Util
import kotlin.math.*
import kotlin.random.Random
import kotlin.random.nextInt

object RegenUtil {
	class GenBounds(val world: World, val x: Int, val z: Int, val width: Int, val depth: Int) {
		companion object {
			fun fromChunk(chunk: Chunk) = GenBounds(
				chunk.world,
				chunk.x * 16,
				chunk.z * 16,
				16,
				16,
			)
		}

		fun centerX() = x + width / 2
		fun centerZ() = z + depth / 2
		fun alongX(t: Float) = (x + width * t).toInt()
		fun alongZ(t: Float) = (z + depth * t).toInt()
		fun randomIn() = x + Random.nextInt(width) to z + Random.nextInt(depth)
	}

	/**
	 * gives block positions at scanY
	 */
	fun <T> locateAround(
		world: World,
		centerX: Int,
		centerZ: Int,
		scanSize: Int,
		minRadius: Double,
		maxRadius: Double,
		maxTries: Int,
		isGood: (x: Int, z: Int) -> T?,
	): ArrayList<T> {
		/*
		 * find biomes at the player's y level
		 * in concentric circles which are each about scanSize larger than the next
		 * starting with radius minRadius, up to radius maxRadius
		 */
		val numCircles = ceil((maxRadius - minRadius) / scanSize).toInt()

		/*
		 * all the points we check in concentric circles
		 * the order of the circles, and the points are randomized
		 */
		val points = (0 until numCircles).flatMap { i ->
			val radius = Util.interp(
				minRadius,
				maxRadius,
				Util.invInterp(0.0, numCircles - 1.0, i.toDouble())
			)

			val circumferenceLength = 2.0 * PI * radius
			val numPoints = (circumferenceLength / scanSize).roundToInt()

			val startAngle = Random.nextDouble(0.0, 2 * PI).toFloat()

			Array(numPoints) { j ->
				val alongCircumference = Util.invInterp(0.0, numPoints.toDouble(), j.toDouble())

				val x = floor(centerX + 0.5 + radius * cos(startAngle + alongCircumference * 2.0 * PI)).toInt()
				val z = floor(centerZ + 0.5 + radius * sin(startAngle + alongCircumference * 2.0 * PI)).toInt()

				x to z
			}.asIterable()
		} as ArrayList<Pair<Int, Int>>
		points.shuffle()

		val ret = ArrayList<T>(maxTries)

		for ((x, z) in points) {
			if (!insideWorldBorder(world, x, z)) continue

			val result = isGood(x, z)
			if (result != null) {
				ret.add(result)
				if (ret.size >= maxTries) return ret
			}
		}

		return ret
	}

	fun <T> sphereAround(
		world: World,
		centerX: Int,
		centerY: Int,
		centerZ: Int,
		minRadius: Float,
		maxRadius: Float,
		tries: Int,
		isGood: (x: Int, y: Int, z: Int) -> T?,
	): ArrayList<T> {
		val ret = ArrayList<T>()
		val lowFraction = minRadius / maxRadius

		for (i in 0 until tries) {
			/* random points on a sphere */
			val theta = Random.nextFloat() * 2.0f * Math.PI.toFloat()
			val phi = acos((2 * Random.nextFloat()) - 1)
			val r = (Random.nextFloat() * (1.0f - lowFraction) + lowFraction).pow(1.0f / 3.0f)

			/* from unit to world coordinates */
			val x = floor(centerX + (r * sin(phi) * cos(theta)) * maxRadius).toInt()
			val y = floor(centerY + (r * sin(phi) * sin(theta)) * maxRadius).toInt()
			val z = floor(centerZ + (r * cos(phi) /*        */) * maxRadius).toInt()

			if (!insideWorldBorder(world, x, z)) continue

			val result = isGood(x, y, z) ?: continue
			ret.add(result)
		}

		return ret
	}

	fun <T> volume(
		bounds: GenBounds,
		yRange: IntRange,
		tries: Int,
		isGood: (block: Block) -> T?,
	): ArrayList<T> {
		val ret = ArrayList<T>(tries)

		for (i in 0 until tries) {
			val y = Random.nextInt(yRange)
			val (x, z) = bounds.randomIn()
			isGood(bounds.world.getBlockAt(x, y, z))?.let { ret.add(it) }
		}

		return ret
	}

	fun yRangeLinear(
		inputY: Float,
		inputLow: Float,
		inputHigh: Float,
		low: Int,
		high: Int,
	): Int {
		return if (inputY >= inputLow && inputY < inputHigh) {
			Util.interp(
				low.toFloat(), high + 1.0f,
				Util.invInterp(inputLow, inputHigh, inputY)
			).toInt()
		} else {
			0
		}
	}

	fun yRangeCenterBias(
		inputY: Float,
		inputLow: Float,
		inputHigh: Float,
		low: Int,
		high: Int,
	): Int {
		return if (inputY >= inputLow && inputY < inputHigh) {
			Util.interp(
				low.toFloat(), high + 1.0f,
				4.0f * (Util.invInterp(inputLow, inputHigh, inputY) - 0.5f).pow(3) + 0.5f
			).toInt()
		} else {
			0
		}
	}

	fun insideWorldBorder(world: World, x: Int, z: Int): Boolean {
		val radius = (world.worldBorder.size / 2.0).toInt()
		return x in -radius..radius && z in -radius..radius
	}

	fun surfaceSpreaderOverworld(
		world: World,
		x: Int,
		z: Int,
		spread: Int,
		isGood: (block: Block) -> Boolean,
	): Block? {
		return surfaceSpreader(world, x, 0, z, spread, ::initialSurfaceOverworld, isGood)
	}

	fun surfaceSpreaderNether(
		world: World,
		x: Int,
		y: Int,
		z: Int,
		spread: Int,
		isGood: (block: Block) -> Boolean,
	): Block? {
		return surfaceSpreader(world, x, y, z, spread, ::initialSurfaceNether, isGood)
	}

	const val MAX_SURFACE = 200
	const val MIN_SURFACE = 58

	fun findSurfaceFromTop(world: World, x: Int, z: Int): Block {
		for (y in MAX_SURFACE downTo MIN_SURFACE + 1) {
			val block = world.getBlockAt(x, y, z)
			if (!surfacePassable(block)) return block
		}
		return world.getBlockAt(x, MIN_SURFACE, z)
	}

	inline fun superSurfaceSpreader(genBounds: GenBounds, isGood: (Block) -> Boolean): ArrayList<Block> {
		val potentialBlocks = ArrayList<Block>()

		for (x in genBounds.x until genBounds.x + genBounds.width) {
			var currentBlock = findSurfaceFromTop(genBounds.world, x, genBounds.z)
			if (isGood(currentBlock)) potentialBlocks.add(currentBlock)

			for (z in genBounds.z + 1 until genBounds.z + genBounds.depth) {
				currentBlock = findSurfaceFromBE(genBounds.world.getBlockAt(x, currentBlock.y, z))
				if (isGood(currentBlock)) potentialBlocks.add(currentBlock)
			}
		}

		return potentialBlocks
	}

	private fun surfaceSpreader(
		world: World,
		x: Int,
		y: Int,
		z: Int,
		spread: Int,
		initialSurface: (world: World, x: Int, y: Int, z: Int) -> Block?,
		isGood: (block: Block) -> Boolean,
	): Block? {
		var initialBlock = world.getBlockAt(x, 0, z)
		/* try ot find initial block */
		for (i in 0..spread) {
			if (i == spread) return null
			val block = initialSurface(
				world,
				x + Random.nextInt(-spread, spread),
				y + Random.nextInt(-spread, spread),
				z + Random.nextInt(-spread, spread)
			)
			if (block != null) {
				initialBlock = block
				break
			}
		}

		if (isGood(initialBlock)) return initialBlock

		val previousBlocks: Array<Block> = Array(8) { initialBlock }

		for (i in 1..spread) {
			for (j in 0 until 8) {
				val (ox, oz) = when (j) {
					0 -> 1 to 0
					1 -> 0 to 1
					2 -> -1 to 0
					3 -> 0 to -1
					4 -> 1 to 1
					5 -> -1 to -1
					6 -> 1 to -1
					else -> -1 to 1
				}

				val previousBlock = previousBlocks[j]

				if (!insideWorldBorder(world, previousBlock.x + ox, previousBlock.z + oz)) continue

				val nextBlock = findSurfaceFrom(previousBlock.getRelative(ox, 0, oz), 20)

				if (nextBlock == null) {
					/* gap continuation */
					previousBlocks[j] = previousBlock.getRelative(ox, 0, oz)
				} else if (isGood(nextBlock)) {
					return nextBlock
				} else {
					previousBlocks[j] = nextBlock
				}
			}
		}

		return null
	}

	/**
	 * expands in ~~26~~ 6 directions from a center block until a good block is found
	 *
	 * @param isGood return true is the block is good, false if the block is not good yet, null if the path is unrecoverable
	 */
	fun expandFrom(centerBlock: Block, range: Int, isGood: (block: Block) -> Boolean?): Block? {
		val previousBlocks: Array<Block?> = Array(26) { centerBlock }

		data class O(val ox: Int, val oy: Int, val oz: Int)

		for (i in 1..range) {
			for (j in 0 until 6) {
				val (ox, oy, oz) = when (j) {
					0 -> O(1, 0, 0)
					1 -> O(0, 1, 0)
					2 -> O(0, 0, 1)
					3 -> O(-1, 0, 0)
					4 -> O(0, -1, 1)
					else -> O(1, 0, -1)
				}

				val previousBlock = previousBlocks[j] ?: continue
				val nextBlock = previousBlock.getRelative(ox, oy, oz)
				val result = isGood(nextBlock)

				if (result == null) {
					/* do not continue down this path */
					previousBlocks[j] = null
				} else if (result) {
					/* a good block is finally found */
					return nextBlock
				} else {
					/* keep going along this path */
					previousBlocks[j] = nextBlock
				}
			}
		}

		return null
	}

	fun newExpandFrom(
		expandFaces: List<BlockFace>,
		centerBlock: Block,
		range: Int,
		isGood: (Block) -> Boolean?
	): Block? {
		val world = centerBlock.world
		val usingFaces = ArrayList(expandFaces)
		for (i in 1..range) {
			val removeFaces = ArrayList<BlockFace>(expandFaces.size)
			for (face in usingFaces) {
				val primaryAxis = IntVector.fromBlockFace(face)
				val orth0 = primaryAxis.orthogonal0()
				val orth1 = primaryAxis.orthogonal1()

				val block = IntVector.fromBlock(centerBlock).add(primaryAxis.mul(i)).block(world)
				when (isGood(block)) {
					true -> return block
					false -> {}
					null -> {
						removeFaces.add(face)
						continue
					}
				}

				for (i in -1 .. 1) {
					for (j in -1 .. 1) {
						if (i == 0 && j == 0) continue
						val block = IntVector.fromBlock(centerBlock).add(primaryAxis.mul(i)).add(orth0.mul(i)).add(orth1.mul(j)).block(world)
						when (isGood(block)) {
							true -> return block
							false -> {}
							null -> {
								removeFaces.add(face)
								continue
							}
						}
					}
				}
			}
			usingFaces.removeAll(removeFaces.toSet())
			if (usingFaces.isEmpty()) return null
		}
		return null
	}

	val SEA_LEVEL = 62
	val ROOF_CHECK = 16
	val START_RANGE = 62..80
	val TOO_LOW = 50

	val surfaceIgnore = Util.sortedArrayOf(
		Material.OAK_LEAVES,
		Material.BIRCH_LEAVES,
		Material.ACACIA_LEAVES,
		Material.SPRUCE_LEAVES,
		Material.JUNGLE_LEAVES,
		Material.DARK_OAK_LEAVES,
		Material.AZALEA_LEAVES,
		Material.FLOWERING_AZALEA_LEAVES,
		Material.RED_MUSHROOM_BLOCK,
		Material.BROWN_MUSHROOM_BLOCK,
		Material.MUSHROOM_STEM,
		Material.OAK_LOG,
		Material.BIRCH_LOG,
		Material.ACACIA_LOG,
		Material.SPRUCE_LOG,
		Material.JUNGLE_LOG,
		Material.DARK_OAK_LOG,
		Material.BAMBOO,
		Material.COCOA,
		Material.LILY_PAD,
		Material.CACTUS,
	)

	fun surfacePassable(block: Block): Boolean {
		return (block.isPassable && !block.isLiquid) || Util.binarySearch(block.type, surfaceIgnore)
	}

	fun initialSurfaceOverworld(world: World, x: Int, y: Int, z: Int): Block? {
		var currentBlock = world.getBlockAt(x, Random.nextInt(START_RANGE), z)

		/* start in air perhaps?, reach down to the ground */
		while (surfacePassable(currentBlock)) {
			currentBlock = currentBlock.getRelative(DOWN)
			if (currentBlock.y <= TOO_LOW) return null
		}

		/* then keep going up */
		while (true) {
			/* the one that should be the surface */
			var surfaceBlock = currentBlock

			while (!surfacePassable(currentBlock)) {
				surfaceBlock = currentBlock
				currentBlock = currentBlock.getRelative(UP)
			}

			/* one last check to make sure we're not in a cave */
			for (i in 1 until ROOF_CHECK) {
				currentBlock = surfaceBlock.getRelative(0, i, 0)
				if (!surfacePassable(currentBlock)) continue
			}

			return surfaceBlock
		}
	}

	fun initialSurfaceNether(world: World, x: Int, y: Int, z: Int): Block? {
		var lastUp = world.getBlockAt(x, y, z)
		var lastDown = lastUp

		for (i in 0 until 20) {
			val up = lastUp.getRelative(UP)
			/* prevent going onto nether roof */
			if (up.y < 127 && !lastUp.isPassable && up.isPassable) {
				return lastUp
			} else {
				lastUp = up
			}

			val down = lastDown.getRelative(DOWN)
			if (lastDown.isPassable && !down.isPassable) {
				return down
			} else {
				lastDown = down
			}
		}

		return null
	}

	fun findSurfaceFrom(block: Block, yRange: Int): Block? {
		var last = block

		if (surfacePassable(block)) {
			/* go down */
			for (i in 1..yRange) {
				val below = block.getRelative(0, -i, 0)
				if (!surfacePassable(below)) return last

				last = below
			}
		} else {
			/* go up */
			for (i in 1..yRange) {
				val above = block.getRelative(0, i, 0)
				if (surfacePassable(above)) return last

				last = above
			}
		}

		return null
	}

	fun findSurfaceFromBE(block: Block): Block {
		var last = block

		if (surfacePassable(block)) {
			/* go down */
			for (y in block.y - 1 downTo MIN_SURFACE) {
				val below = block.world.getBlockAt(block.x, y, block.z)
				if (!surfacePassable(below)) return below

				last = below
			}
		} else {
			/* go up */
			for (y in block.y + 1..MAX_SURFACE) {
				val above = block.world.getBlockAt(block.x, y, block.z)
				if (surfacePassable(above)) return last

				last = above
			}
		}

		return last
	}
}

