package com.codeland.uhc.world.chunkPlacer

import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.block.Block
import kotlin.random.Random

abstract class AbstractChunkPlacer(val size: Int) {
	abstract fun place(chunk: Chunk)
	abstract fun onGenerate(chunk: Chunk, uniqueSeed: Long, worldSeed: Long)

	companion object {
		val random = Random(System.currentTimeMillis())

		fun shouldGenerate(chunkX: Int, chunkZ: Int, seed0: Long, seed1: Long, size: Int): Boolean {
			if (size == 1) return true

			val baseX = Util.floorDiv(chunkX, size)
			val baseZ = Util.floorDiv(chunkZ, size)

			val random = Random(
				seed0.xor(seed1.shl(32).or(seed1.ushr(32))).xor(baseX.toLong())
					.xor(baseZ.toLong().shl(32).or(baseZ.toLong().ushr(32)))
			)

			/* indexed by bits to see if this spot has been filled */
			var filled = 0

			/* try n out of n*n spots, equates to 1/n chance of finding */
			for (i in 0 until size) {
				var spot = random.nextInt(size * size)
				while (filled.ushr(spot).and(1) == 1) spot = (spot + 1) % (size * size)
				filled = filled.or(1.shl(spot))

				/* found the index of this subchunk */
				if (spot == Util.mod(chunkZ, size) * size + Util.mod(chunkX, size)) {
					return true
				}
			}

			return false
		}

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
	}
}