package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.data.Bisected
import javax.swing.text.MutableAttributeSet

class WaterPlacer(size: Int, uniqueSeed: Int) : ImmediateChunkPlacer(size, uniqueSeed) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 0..127) {
					val block = chunk.getBlock(x, y, z)

					when {
						block.type == Material.GRASS -> {
							block.setType(Material.SEAGRASS, false)
						}
						block.type == Material.TALL_GRASS -> {
							val half = (block.blockData as Bisected).half
							block.setType(Material.TALL_SEAGRASS, false)
							(block.blockData as Bisected).half = half
						}
						block.type == Material.LAVA -> {}
						block.type == Material.SUGAR_CANE -> {}
						block.type == Material.SEAGRASS -> {}
						block.type == Material.KELP_PLANT -> {}
						block.type == Material.GRASS_BLOCK -> block.setType(Material.SAND, false)
						block.isPassable -> {
							block.setType(Material.WATER, false)
						}
					}

					block.biome = Biome.WARM_OCEAN
				}
			}
		}
	}
}
