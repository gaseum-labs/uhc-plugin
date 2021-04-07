package com.codeland.uhc.world.gen

import net.minecraft.server.v1_16_R3.*

class WorldChunkManagerOverworldNoOcean(var0: Long, var2: Boolean, var3: Boolean, private val var4: IRegistry<BiomeBase>) : WorldChunkManagerOverworld(var0, var2, var3, var4) {
    val oceans = arrayOf(
        Biomes.OCEAN,
        Biomes.COLD_OCEAN,
        Biomes.DEEP_COLD_OCEAN,
        Biomes.DEEP_FROZEN_OCEAN,
        Biomes.DEEP_LUKEWARM_OCEAN,
        Biomes.DEEP_OCEAN,
        Biomes.FROZEN_OCEAN,
        Biomes.WARM_OCEAN,
        Biomes.LUKEWARM_OCEAN
    )

    override fun getBiome(var0: Int, var1: Int, var2: Int): BiomeBase {
        val biome = super.getBiome(var0, var1, var2)
        return if (oceans.any { key -> (var4.d(key) as BiomeBase) == biome }) {
            var4.d(Biomes.PLAINS)
        } else {
            biome
        }
    }
}