package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.UP
import org.gaseumlabs.uhc.util.Util
import kotlin.math.*
import kotlin.random.Random
import kotlin.random.nextInt

object RegenUtil {
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

	fun insideWorldBorder(world: World, x: Int, z: Int): Boolean {
		val radius = (world.worldBorder.size / 2.0).toInt()
		return x in -radius..radius && z in -radius..radius
	}

	fun surfaceSpreader(
		world: World,
		x: Int,
		z: Int,
		spread: Int,
		isGood: (block: Block) -> Boolean,
	): Block? {
		var initialBlock: Block?
		do {
			initialBlock = findInitialSurfaceAt(
				world,
				x + Random.nextInt(-spread, spread),
				z + Random.nextInt(-spread, spread)
			) ?: return null
		} while (initialBlock == null)

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
	 * expands in a 26 directions from a center block until a good block is found
	 *
	 * @param isGood return true is 4the block is good, false if the block is not good yet, null if the path is unrecoverable
	 */
	fun expandFrom(centerBlock: Block, range: Int, isGood: (block: Block) -> Boolean?): Block? {
		val previousBlocks: Array<Block?> = Array(26) { centerBlock }

		data class O(val ox: Int, val oy: Int, val oz: Int)

		for (i in 1..range) {
			for (j in 0 until 26) {
				val (ox, oy, oz) = when (j) {
					0 -> O(1, 0, 0)
					1 -> O(0, 1, 0)
					2 -> O(0, 0, 1)
					3 -> O(1, 1, 0)
					4 -> O(0, 1, 1)
					5 -> O(1, 0, 1)
					6 -> O(1, 1, 1)
					7 -> O(-1, 0, 0)
					8 -> O(0, -1, 0)
					9 -> O(0, 0, -1)
					10 -> O(-1, -1, 0)
					11 -> O(0, -1, -1)
					12 -> O(-1, 0, -1)
					13 -> O(-1, -1, -1)
					14 -> O(1, -1, 0)
					15 -> O(-1, 1, 0)
					16 -> O(0, 1, -1)
					17 -> O(0, -1, 1)
					18 -> O(1, 0, -1)
					19 -> O(-1, 0, 1)
					20 -> O(1, 1, -1)
					21 -> O(1, -1, 1)
					22 -> O(-1, 1, 1)
					23 -> O(-1, -1, 1)
					24 -> O(-1, 1, -1)
					else -> O(1, -1, -1)
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

	val SEA_LEVEL = 62
	val ROOF_CHECK = 16
	val START_RANGE = 62..80
	val TOO_LOW = 50

	val treeParts = arrayOf(
		Material.OAK_LEAVES,
		Material.BIRCH_LEAVES,
		Material.ACACIA_LEAVES,
		Material.SPRUCE_LEAVES,
		Material.JUNGLE_LEAVES,
		Material.DARK_OAK_LEAVES,
		Material.RED_MUSHROOM_BLOCK,
		Material.BROWN_MUSHROOM_BLOCK,
		Material.MUSHROOM_STEM,
		Material.OAK_LOG,
		Material.BIRCH_LOG,
		Material.ACACIA_LOG,
		Material.SPRUCE_LOG,
		Material.JUNGLE_LOG,
		Material.DARK_OAK_LOG,
	)

	fun surfacePassable(block: Block): Boolean {
		return (block.isPassable && !block.isLiquid) || treeParts.contains(block.type)
	}

	fun findInitialSurfaceAt(world: World, x: Int, z: Int): Block? {
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
}

