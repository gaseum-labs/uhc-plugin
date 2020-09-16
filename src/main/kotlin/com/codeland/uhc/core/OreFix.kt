package com.codeland.uhc.core

import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.BlockFace
import kotlin.math.abs
import kotlin.math.sin

object OreFix {
	val fixedOres = arrayOf(
		Material.GOLD_ORE,
		Material.LAPIS_ORE,
		Material.DIAMOND_ORE
	)

	init {
		fixedOres.sort()
	}

	fun removeOres(chunk: Chunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 1..127) {
					val block = chunk.getBlock(x, y, z)
					if (Util.binarySearch(block.type, fixedOres)) {
						block.setType(Material.STONE, false)
					}
				}
			}
		}
	}

	fun addOres(chunk: Chunk, seed: Int) {
		if (shouldGenerate(chunk.x, chunk.z, seed, 3247892,  3)) addOre(chunk, 10, 32, Material.GOLD_ORE)
		if (shouldGenerate(chunk.x, chunk.z, seed, 9837,     4)) addOre(chunk, 10, 32, Material.LAPIS_ORE)
		if (shouldGenerate(chunk.x, chunk.z, seed, 572919,   5)) addOre(chunk, 10, 14, Material.DIAMOND_ORE)
	}

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
			if (spot == (chunkX % size) * size + (chunkZ % size)) return true
		}

		return false
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

	fun addOre(chunk: Chunk, low: Int, high: Int, type: Material) {
		val height = high - low + 1

		val size = 14 * 14 * height
		val offset = (Math.random() * size).toInt()

		for (i in 0 until size) {
			val x = ((i + offset) % 14) + 1
			val z = (((i + offset) / 14) % 14) + 1
			val y = (((i + offset) / (14 * 14)) % height) + low

			val block = chunk.getBlock(x, y, z)
			if (canReplace(block.type)) {
				if (
					block.getRelative(BlockFace.DOWN).type == Material.CAVE_AIR ||
					block.getRelative(BlockFace.UP).type == Material.CAVE_AIR ||
					block.getRelative(BlockFace.EAST).type == Material.CAVE_AIR ||
					block.getRelative(BlockFace.WEST).type == Material.CAVE_AIR ||
					block.getRelative(BlockFace.NORTH).type == Material.CAVE_AIR ||
					block.getRelative(BlockFace.SOUTH).type == Material.CAVE_AIR
				) {
					val offX = if (Math.random() < 0.5) -1 else 0
					val offY = if (Math.random() < 0.5) -1 else 0
					val offZ = if (Math.random() < 0.5) -1 else 0

					for (shiftX in 0 until 2) {
						for (shiftY in 0 until 2) {
							for (shiftZ in 0 until 2) {
								val block = chunk.getBlock(x + offX + shiftX, y + offY + shiftY, z + offZ + shiftZ)
								if (canReplace(block.type) && Math.random() < 0.75) block.setType(type, false)
							}
						}
					}

					break
				}
			}
		}
	}
}