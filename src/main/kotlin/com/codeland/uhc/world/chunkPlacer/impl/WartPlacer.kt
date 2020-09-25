package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable

class WartPlacer(size: Int, uniqueSeed: Int) : ImmediateChunkPlacer(size, uniqueSeed) {
	override fun place(chunk: Chunk) {
		randomPosition(chunk, 32, 99) { block, x, y, z ->
			val world = block.world
			val under = block.getRelative(BlockFace.DOWN)

			fun placeWart(check: (Block, Block) -> Boolean): Boolean {
				return if (check(block, under)) {
					block.type = Material.NETHER_WART

					val data = block.blockData
					if (data is Ageable) {
						data.age = Util.randRange(0, data.maximumAge)
						block.blockData = data
					}

					under.type = Material.SOUL_SAND

					true
				} else {
					false
				}
			}

			when (world.getBiome(chunk.x * 16 + x, y, chunk.z * 16 + z)) {
				Biome.NETHER_WASTES -> placeWart { top, bottom ->
					top.type == Material.AIR && bottom.type == Material.SOUL_SAND
				}
				Biome.BASALT_DELTAS -> placeWart { top, bottom ->
					top.type == Material.AIR && bottom.type == Material.MAGMA_BLOCK
				}
				Biome.CRIMSON_FOREST -> placeWart { top, bottom ->
					top.type == Material.CRIMSON_ROOTS || top.type == Material.CRIMSON_FUNGUS
				}
				Biome.WARPED_FOREST -> placeWart { top, bottom ->
					top.type == Material.WARPED_ROOTS || top.type == Material.WARPED_FUNGUS
				}
				Biome.SOUL_SAND_VALLEY -> placeWart { top, bottom ->
					top.type == Material.AIR && bottom.type == Material.SOUL_SAND
				}
				else -> false
			}
		}
	}
}
