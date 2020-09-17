package com.codeland.uhc.chunkPlacer

import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.BlockFace

class OrePlacer(size: Int, uniqueSeed: Int, val low: Int, val high: Int, val min: Int, val max: Int, val type: Material) : ChunkPlacer(size, uniqueSeed) {
	override fun onGenerate(chunk: Chunk) {
		randomPosition(chunk, 1, low, high) { block, x, y, z ->
			if (canReplace(block.type)) {
				if (
					isOpen(block.getRelative(BlockFace. DOWN).type) ||
					isOpen(block.getRelative(BlockFace.   UP).type) ||
					isOpen(block.getRelative(BlockFace. EAST).type) ||
					isOpen(block.getRelative(BlockFace. WEST).type) ||
					isOpen(block.getRelative(BlockFace.NORTH).type) ||
					isOpen(block.getRelative(BlockFace.SOUTH).type)
				) {
					val amount = (hash3(chunk.x, chunk.z, uniqueSeed) * (max - min)).toInt() + min

					val positions = Array(amount) { Triple(0, 0, 0) }

					/* place the first block of ore */
					positions[0] = Triple(x, y, z)
					block.setType(type, false)

					/* place the rest in a contiguous cluster */
					for (j in 1 until amount) {
						/* get an existing position */
						var positionIndex = Util.randRange(0, j - 1)
						val startIndex = positionIndex

						var nextPosition: Triple<Int, Int, Int>? = null

						while (nextPosition == null) {
							val thisPosition = positions[positionIndex]
							nextPosition = placeBlock(chunk, thisPosition.first, thisPosition.second, thisPosition.third)

							if (nextPosition == null) {
								positionIndex = (positionIndex + 1) % amount
								/* no more ores can be placed contiguously */
								if (positionIndex == startIndex) return@randomPosition true
							}
						}

						positions[j] = nextPosition
					}

					return@randomPosition true
				}
			}

			false
		}

		//DEBUG
		//for (x in 0..15) {
		//	for (z in 0..15) {
		//		if (Math.random() < 0.3) chunk.getBlock(x, 200, z).setType(type, false)
		//	}
		//}
	}

	/**
	 * @return null the block couldn't be placed
	 */
	private fun placeBlock(chunk: Chunk, x: Int, y: Int, z: Int): Triple<Int, Int, Int>? {
		val block = chunk.getBlock(x, y, z)

		var faceIndex = Util.randRange(0, 5)
		val startIndex = faceIndex
		var blockFace = BlockFace.values()[faceIndex]

		while (
			outOfBounds(x + blockFace.modX, y + blockFace.modY, z + blockFace.modZ) ||
			!canReplace(block.getRelative(blockFace).type)
		) {
			faceIndex = (faceIndex + 1) % 6
			/* every single face is covered */
			if (faceIndex == startIndex) return null

			blockFace = BlockFace.values()[faceIndex]
		}

		block.getRelative(blockFace).setType(type, false)

		/* the chunk position of the newly placed block */
		return Triple(x + blockFace.modX, y + blockFace.modY, z + blockFace.modZ)
	}

	private fun outOfBounds(x: Int, y: Int, z: Int): Boolean {
		return x < 0 || x > 15 || y < 0 || y > 255 || z < 0 || z > 15
	}

	private fun isOpen(type: Material): Boolean {
		return when (type) {
			Material.AIR -> true
			Material.CAVE_AIR -> true
			Material.WATER -> true
			else -> false
		}
	}

	private fun canReplace(type: Material): Boolean {
		return when (type) {
			Material.STONE -> true
			Material.ANDESITE -> true
			Material.DIORITE -> true
			Material.GRANITE -> true
			else -> false
		}
	}
}
