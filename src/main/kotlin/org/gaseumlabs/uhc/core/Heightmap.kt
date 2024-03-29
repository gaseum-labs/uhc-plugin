package org.gaseumlabs.uhc.core

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.gaseumlabs.uhc.world.regenresource.RegenUtil

class Heightmap(val radius: Int, val range: Int) {
	val heights = IntArray((radius * 2 + 1) * (radius * 2 + 1))
	var heightLimit = 0

	companion object {
		val MIN_HEIGHT = 61
		val MAX_HEIGHT = 255
		val CHECK_ABOVE = 5
	}

	private fun ignore(block: Block): Boolean {
		return block.isPassable ||
			block.type === Material.ICE ||
			RegenUtil.surfaceIgnore.contains(block.type)
	}

	private fun topOfColumnFrom(world: World, x: Int, initialY: Int, z: Int): Int {
		for (y in initialY until MAX_HEIGHT) {
			if (ignore(world.getBlockAt(x, y, z))) {
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
				if (!ignore(world.getBlockAt(x, y, z))) {
					runningTop = y
					terrainAbove = true
					break
				}
			}

			if (!terrainAbove) break
		}

		return runningTop - 1
	}

	fun columnIndex(x: Int, z: Int): Int {
		val width = radius * 2 + 1
		return (z + radius) * width + (x + radius)
	}

	fun generate(world: World) {
		for (x in -radius..radius) {
			for (z in -radius..radius) {
				heights[columnIndex(x, z)] = columnHeight(world, x, z)
			}
		}

		val minHeight = heights.minOrNull()!!
		heightLimit = minHeight + range

		for (i in heights.indices) {
			if (heights[i] > heightLimit - 1) {
				heights[i] = heightLimit - 1
			}
		}
	}

	fun get(x: Int, z: Int): Int {
		return heights[columnIndex(x, z)]
	}
}
