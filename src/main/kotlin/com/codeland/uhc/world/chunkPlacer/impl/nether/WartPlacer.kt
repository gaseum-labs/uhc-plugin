package com.codeland.uhc.world.chunkPlacer.impl.nether

import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.*
import org.bukkit.block.data.Ageable

class WartPlacer(size: Int) : ChunkPlacer(size) {
	override fun place(chunk: Chunk) {
		randomPositionBool(chunk, 32, 99) { block ->
			val under = block.getRelative(BlockFace.DOWN)

			fun placeWart(check: (Block, Block) -> Boolean): Boolean {
				return if (check(block, under)) {
					block.setType(Material.NETHER_WART, false)

					val data = block.blockData as Ageable
					data.age = data.maximumAge
					block.blockData = data

					under.setType(Material.SOUL_SAND, false)

					true

				} else {
					false
				}
			}

			when (block.biome) {
				Biome.NETHER_WASTES -> placeWart { top, bottom ->
					top.type.isAir && bottom.type == Material.SOUL_SAND
				}
				Biome.BASALT_DELTAS -> placeWart { top, bottom ->
					top.type.isAir && bottom.type == Material.MAGMA_BLOCK
				}
				Biome.CRIMSON_FOREST -> placeWart { top, bottom ->
					top.isPassable && bottom.type == Material.CRIMSON_NYLIUM
				}
				Biome.WARPED_FOREST -> placeWart { top, bottom ->
					top.isPassable && bottom.type == Material.WARPED_NYLIUM
				}
				Biome.SOUL_SAND_VALLEY -> placeWart { top, bottom ->
					top.type.isAir && bottom.type == Material.SOUL_SAND
				}
				else -> false
			}
		}
	}
}
