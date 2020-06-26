package com.codeland.uhc.world

import nl.rutgerkok.worldgeneratorapi.BaseChunkGenerator.*
import nl.rutgerkok.worldgeneratorapi.BaseTerrainGenerator
import org.bukkit.block.Biome

class UHCTerrainGenerator : BaseTerrainGenerator {
    private val oceanMap = mapOf(
            Biome.WARM_OCEAN to Biome.BADLANDS,

            Biome.LUKEWARM_OCEAN to Biome.DARK_FOREST,
            Biome.DEEP_LUKEWARM_OCEAN to Biome.JUNGLE,

            Biome.OCEAN to Biome.PLAINS,
            Biome.DEEP_OCEAN to Biome.FOREST,

            Biome.COLD_OCEAN to Biome.MOUNTAINS,
            Biome.DEEP_COLD_OCEAN to Biome.TAIGA,

            Biome.FROZEN_OCEAN to Biome.SNOWY_TUNDRA,
            Biome.DEEP_FROZEN_OCEAN to Biome.SNOWY_TAIGA
    )

    override fun setBlocksInChunk(chunk: GeneratingChunk) {
        val biome = chunk.biomesForChunk
        for (x in 0..15) {
            for (z in 0..15) {
                val currentBiome = biome.getBiome(x, 0, z)
                if (oceanMap.containsKey(currentBiome)) {
                    for (y in 0..255) {
                        biome.setBiome(x, y, z, oceanMap.getValue(currentBiome))
                    }
                }
            }
        }
    }

}