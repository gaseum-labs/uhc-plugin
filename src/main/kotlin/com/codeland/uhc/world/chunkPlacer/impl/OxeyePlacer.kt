package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace

class OxeyePlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, 63, 99) { block, x, y, z ->
			if ((block.biome == Biome.PLAINS || block.biome == Biome.FLOWER_FOREST) && block.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK && block.type == Material.AIR) {
				block.setType(Material.OXEYE_DAISY, false)
				true

			} else {
				false
			}
		}
	}

	companion object {
		fun removeOxeye(chunk: Chunk) {
			for (x in 0..15) {
				for (z in 0..15) {
					for (y in 63..99) {
						val block = chunk.getBlock(x, y, z)
						if (block.type == Material.OXEYE_DAISY) block.setType(Material.AIR, false)
					}
				}
			}
		}
	}
}
