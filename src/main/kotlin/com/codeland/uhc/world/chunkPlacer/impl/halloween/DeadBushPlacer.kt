package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import kotlin.random.Random

class DeadBushPlacer(size: Int) : ChunkPlacer(size) {
	override fun place(chunk: Chunk) {
		val numBushes = Random.nextInt(0, 4)

		for (i in 0 until numBushes) {
			val x = Random.nextInt(0, 16)
			val z = Random.nextInt(0, 16)
			val y = findBushY(chunk, x, z)

			if (y != -1) {
				chunk.getBlock(x, y, z).setType(Material.DEAD_BUSH, false)
				chunk.getBlock(x, y - 1, z).setType(Material.COARSE_DIRT, false)
			}
		}
	}

	private fun findBushY(chunk: Chunk, x: Int, z: Int): Int {
		for (y in 85 downTo 60) {
			val block = chunk.getBlock(x, y, z)

			if (
				block.type == Material.GRASS ||
				block.type == Material.FERN
			) return y

			if (
				block.type == Material.GRASS_BLOCK ||
				block.type == Material.DIRT ||
				block.type == Material.COARSE_DIRT
			) return y + 1

			if (block.type != Material.AIR) return -1
		}

		return -1
	}
}
