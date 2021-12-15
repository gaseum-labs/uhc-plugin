package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.Material.GRASS
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace

class OxeyePlacer(size: Int) : ChunkPlacer(size) {
	override fun place(chunk: Chunk) {
		randomPositionBool(chunk, 63, 99) { block ->
			(block.biome === Biome.PLAINS || block.biome === Biome.FLOWER_FOREST) &&
			block.getRelative(BlockFace.DOWN).type === Material.GRASS_BLOCK &&
			(block.type.isAir || block.type === GRASS)
		}?.setType(Material.OXEYE_DAISY, false)
	}

	companion object {
		fun removeOxeye(chunk: Chunk) {
			for (x in 0..15) {
				for (z in 0..15) {
					for (y in 63..99) {
						val block = chunk.getBlock(x, y, z)
						if (block.type === Material.OXEYE_DAISY) block.setType(Material.AIR, false)
					}
				}
			}
		}
	}
}
