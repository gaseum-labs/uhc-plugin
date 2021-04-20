package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material

class DeadBushPlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		val numBushes = Util.randRange(0, 3)

		for (i in 0 until numBushes) {
			val x = Util.randRange(0, 15)
			val z = Util.randRange(0, 15)
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
