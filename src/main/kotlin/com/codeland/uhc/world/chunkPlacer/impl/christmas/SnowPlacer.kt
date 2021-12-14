package com.codeland.uhc.world.chunkPlacer.impl.christmas

import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace

class SnowPlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				var placedSnow = false

				for (y in 255 downTo 0) {
					val block = chunk.getBlock(x, y, z)

					if (!placedSnow) {
						if ((block.type != Material.AIR && block.type != Material.CAVE_AIR)) {
							if (!block.isPassable) {
								block.getRelative(BlockFace.UP).setType(Material.SNOW, false)
							}

							placedSnow = true
						}
					}

					block.biome = Biome.SNOWY_TUNDRA
				}
			}
		}
	}
}
