package com.codeland.uhc.chunkPlacer

import com.codeland.uhc.chunkPlacer.ChunkPlacer
import com.codeland.uhc.core.NetherFix
import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable

class WartPlacer(size: Int, uniqueSeed: Int) : ChunkPlacer(size, uniqueSeed) {
	override fun onGenerate(chunk: Chunk) {
		randomPosition(chunk, 0, 32, 99) { block, x, y, z ->
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
