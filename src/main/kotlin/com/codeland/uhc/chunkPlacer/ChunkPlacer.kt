package com.codeland.uhc.chunkPlacer

import com.codeland.uhc.util.Util.mod
import org.bukkit.Chunk
import org.bukkit.block.Block
import kotlin.math.abs
import kotlin.math.sin

abstract class ChunkPlacer(val size: Int, val uniqueSeed: Int) {

	abstract fun onGenerate(chunk: Chunk)

	fun place(chunk: Chunk, seed: Int) {
		if (shouldGenerate(chunk.x, chunk.z, seed, uniqueSeed, size)) onGenerate(chunk)
	}

	companion object {
		fun shouldGenerate(chunkX: Int, chunkZ: Int, seed0: Int, seed1: Int, size: Int): Boolean {
			val regionSeed = hashToInt(hash4(chunkX / size, chunkZ / size, seed0, seed1))

			/* which chunks in this region can generate */
			val generates = Array(size * size) { false }

			var lastRandom = hash3(regionSeed, seed0, seed1)
			for (i in 0 until size) {
				lastRandom = hash4(regionSeed, hashToInt(lastRandom), seed0, seed1)

				var spot = (lastRandom * size * size).toInt()
				while (generates[spot]) spot = (spot + 1) % (size * size)

				generates[spot] = true

				/* region subchunk positions converted to 1d array index */
				if (spot == mod(chunkX, size) * size + mod(chunkZ, size)) return true
			}

			return false
		}

		/* math helpers */

		fun fract(num: Double): Double {
			val iPart = num.toLong()
			return abs(num - iPart)
		}

		fun hash2(seed0: Int, seed1: Int): Double {
			return fract(sin(seed0 * 67.976 + seed1 * 10.6464) * 29549.1183)
		}

		fun hash3(seed0: Int, seed1: Int, seed2: Int): Double {
			return fract(sin(seed0 * 12.873 + seed1 * 54.9062 + seed2 * 64.398) * 17502.9348)
		}

		fun hash4(seed0: Int, seed1: Int, seed2: Int, seed3: Int): Double {
			return fract(sin(seed0 * 23.1947 + seed1 * 50.682 + seed2 * 76.5308 + seed3 * 14.291) * 83720.5964)
		}

		fun hashToInt(hash: Double): Int {
			return (hash * Integer.MAX_VALUE).toInt()
		}

		fun randomPosition(chunk: Chunk, buffer: Int, low: Int, high: Int, placeBlock: (block: Block, x: Int, y: Int, z: Int) -> Boolean) {
			val height = high - low + 1
			val width = 16 - buffer * 2

			val size = width * width * height
			val offset = (Math.random() * size).toInt()

			for (i in 0 until size) {
				val x = ((i + offset) % width) + buffer
				val z = (((i + offset) / width) % width) + buffer
				val y = (((i + offset) / (width * width)) % height) + low

				if (placeBlock(chunk.getBlock(x, y, z), x, y, z)) return
			}
		}
	}
}
