package com.codeland.uhc.world.gen

import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.util.Util
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import kotlin.math.floor
import kotlin.random.Random

class WorldChunkManagerOverworldPvp(
	val seed: Long,
	private val biomeRegistry: IRegistry<BiomeBase>,
) : WorldChunkManagerOverworld(seed, false, false, biomeRegistry) {
	private val inBetween: BiomeBase = biomeRegistry.d(BiomeRegistry.a(BiomeNo.BEACH))

	private val stride = PvpGameManager.ARENA_STRIDE

	private val areaLazy = CustomGenLayers.createGenLayerPvp(seed)

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
	    return when {
	    	PvpGameManager.onEdge(x * 4, z * 4) -> inBetween
		    else -> biomeRegistry.d(BiomeRegistry.a(areaLazy.a(x, z)))
	    }
    }
}
