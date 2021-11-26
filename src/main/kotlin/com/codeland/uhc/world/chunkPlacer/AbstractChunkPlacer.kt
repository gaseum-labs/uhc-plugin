package com.codeland.uhc.world.chunkPlacer

import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.block.Block
import kotlin.math.floor
import kotlin.random.Random

abstract class AbstractChunkPlacer(val size: Int) {
	var uniqueSeed = 0

	abstract fun place(chunk: Chunk, chunkIndex: Int)

	abstract fun onGenerate(chunk: Chunk, seed: Int)

	open fun reset(uniqueSeed: Int) {
		this.uniqueSeed = uniqueSeed
	}

	companion object {
		val random = Random(System.currentTimeMillis())

		/**
		 * @return -1 if this chunk should not generate
		 * if this chunk should generate, returns the index of the chunk within the range
		 */
		fun shouldGenerate(chunkX: Int, chunkZ: Int, seed0: Int, seed1: Int, size: Int): Int {
			if (size == 1) return 0

			val baseX = floor(chunkX / size.toFloat()).toInt()
			val baseZ = floor(chunkZ / size.toFloat()).toInt()

			val seededRandom = Random(seed0.xor(seed1).xor(baseX).xor(baseZ.shl(16)))

			/* which chunks in this region can generate */
			val generates = BooleanArray(size * size)

			for (i in 0 until size) {
				var spot = seededRandom.nextInt(0, size * size)
				while (generates[spot]) spot = (spot + 1) % (size * size)
				generates[spot] = true

				/* region subchunk positions converted to 1d array index */
				if (spot == Util.mod(chunkX, size) * size + Util.mod(chunkZ, size)) return spot
			}

			return -1
		}

		private val xPos = Array(16) { it }
		private val zPos = Array(16) { it }

		fun randomPosition(
			chunk: Chunk,
			low: Int,
			high: Int,
			placeBlock: (block: Block, x: Int, y: Int, z: Int) -> Boolean,
		): Block? {
			xPos.shuffle()
			zPos.shuffle()

			val height = high - low + 1

			val yPos = Array(height) { it + low }
			yPos.shuffle()

			for (k in 0 until height) {
				for (j in 0..15) {
					for (i in 0..15) {
						val y = yPos[k]
						val z = zPos[j]
						val x = xPos[i]

						val block = chunk.getBlock(x, y, z)
						if (placeBlock(block, x, y, z)) return block
					}
				}
			}

			return null
		}
	}
}