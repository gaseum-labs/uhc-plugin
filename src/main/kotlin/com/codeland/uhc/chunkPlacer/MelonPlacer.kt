package com.codeland.uhc.chunkPlacer

import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace

class MelonPlacer(size: Int, uniqueSeed: Int) : ChunkPlacer(size, uniqueSeed) {
	override fun onGenerate(chunk: Chunk) {
		randomPosition(chunk, 1, 63, 99) { block, x, y, z ->
			val world = block.world

			if (
				Util.binarySearch(world.getBiome(chunk.x * 16 + x, 60, chunk.z * 16 + z), allowedBiomes) &&
				block.type == Material.AIR &&
				block.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK
			) {
				block.setType(Material.MELON, false)
				true
			} else {
				false
			}
		}
	}

	val allowedBiomes = arrayOf(
		Biome.PLAINS,
		Biome.SUNFLOWER_PLAINS,
		Biome.FOREST,
		Biome.FLOWER_FOREST,
		Biome.BIRCH_FOREST,
		Biome.TALL_BIRCH_FOREST,
		Biome.DARK_FOREST,
		Biome.DARK_FOREST_HILLS,
		Biome.SWAMP,
		Biome.SWAMP_HILLS,
		Biome.RIVER,
		Biome.BEACH,
		Biome.MUSHROOM_FIELDS,
		Biome.MUSHROOM_FIELD_SHORE
	)
}
