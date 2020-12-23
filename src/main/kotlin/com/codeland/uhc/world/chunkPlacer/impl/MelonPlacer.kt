package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace

class MelonPlacer(size: Int, uniqueSeed: Int) : ImmediateChunkPlacer(size, uniqueSeed) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, 63, 99) { block, x, y, z ->
			val world = block.world

			if (
				Util.binarySearch(world.getBiome(chunk.x * 16 + x, y, chunk.z * 16 + z), allowedBiomes) &&
				block.type == Material.AIR &&
				block.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK
			) {
				block.setType(Material.MELON, false)
				block.getRelative(BlockFace.DOWN).setType(Material.DIRT, false)
				true
			} else {
				false
			}
		}
	}

	val allowedBiomes = arrayOf(
		Biome.FOREST,
		Biome.WOODED_HILLS,
		Biome.FLOWER_FOREST,
		Biome.BIRCH_FOREST,
		Biome.BIRCH_FOREST_HILLS,
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

	init {
		allowedBiomes.sort()
	}
}
