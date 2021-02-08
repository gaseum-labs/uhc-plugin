package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material

class WaterPlacer(size: Int, uniqueSeed: Int) : ImmediateChunkPlacer(size, uniqueSeed) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 0..255) {
					val block = chunk.getBlock(x, y, z)

					if (block.type == Material.AIR || block.type == Material.CAVE_AIR)
						block.setType(Material.WATER, false)
				}
			}
		}
	}
}
