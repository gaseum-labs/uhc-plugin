package com.codeland.uhc.world

import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld
import org.bukkit.generator.ChunkGenerator
import java.util.*

class VanillaChunkGenerator : ChunkGenerator() {
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

    var chunkGenerator: ChunkGenerator? = null

    override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biome: BiomeGrid): ChunkData {

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

        return chunkGenerator!!.generateChunkData(world, random, chunkX, chunkZ, biome)
    }
}