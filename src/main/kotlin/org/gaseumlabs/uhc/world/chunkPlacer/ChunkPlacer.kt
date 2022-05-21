package org.gaseumlabs.uhc.world.chunkPlacer

import org.gaseumlabs.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.block.Block
import kotlin.random.Random

abstract class ChunkPlacer(val perSquare: Int, val squareSize: Int) {
	abstract fun place(chunk: Chunk)

	fun shouldGenerate(chunkX: Int, chunkZ: Int, uniqueSeed: Long, worldSeed: Long): Boolean {
		if (squareSize == 1) return true

		val baseX = Util.floorDiv(chunkX, squareSize)
		val baseZ = Util.floorDiv(chunkZ, squareSize)

		val random = Random(
			uniqueSeed.xor(worldSeed.shl(32).or(worldSeed.ushr(32))).xor(baseX.toLong())
				.xor(baseZ.toLong().shl(32).or(baseZ.toLong().ushr(32)))
		)

		/* indexed by bits to see if this spot has been filled */
		var filled = 0

		/* try n out of n*n spots, equates to 1/n chance of finding */
		for (i in 0 until perSquare) {
			var spot = random.nextInt(squareSize * squareSize)
			while (filled.ushr(spot).and(1) == 1) spot = (spot + 1) % (squareSize * squareSize)
			filled = filled.or(1.shl(spot))

			/* found the index of this subchunk */
			if (spot == Util.mod(chunkZ, squareSize) * squareSize + Util.mod(chunkX, squareSize)) {
				return true
			}
		}

		return false
	}

	companion object {
		private val xPos = Array(16) { it }
		private val zPos = Array(16) { it }

		fun <T> randomPosition(
			chunk: Chunk,
			low: Int,
			high: Int,
			onBlock: (Block) -> T?,
		): T? {
			xPos.shuffle()
			zPos.shuffle()

			val height = high - low + 1

			val yPos = Array(height) { it + low }
			yPos.shuffle()

			for (k in 0 until height) {
				for (j in 0..15) {
					for (i in 0..15) {
						val result = onBlock(chunk.getBlock(xPos[i], yPos[k], zPos[j]))
						if (result != null) return result
					}
				}
			}

			return null
		}

		fun randomPositionBool(
			chunk: Chunk,
			low: Int,
			high: Int,
			onBlock: (Block) -> Boolean,
		): Block? {
			xPos.shuffle()
			zPos.shuffle()

			val height = high - low + 1

			val yPos = Array(height) { it + low }
			yPos.shuffle()

			for (k in 0 until height) {
				for (j in 0..15) {
					for (i in 0..15) {
						val block = chunk.getBlock(xPos[i], yPos[k], zPos[j])
						if (onBlock(block)) return block
					}
				}
			}

			return null
		}

		fun randomSinglePosition(chunk: Chunk, low: Int, high: Int): Block {
			return chunk.getBlock(Random.nextInt(16), Random.nextInt(low, high + 1), Random.nextInt(16))
		}
	}
}