package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.world.gen.BiomeNo
import com.codeland.uhc.world.gen.CustomGenLayers
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld

class WorldChunkManagerOverworldPvp(
	val seed: Long,
	private val biomeRegistry: IRegistry<BiomeBase>,
) : WorldChunkManagerOverworld(seed, false, false, biomeRegistry) {
	private val inBetween: BiomeBase = biomeRegistry.d(BiomeRegistry.a(BiomeNo.BEACH))

	private val areaLazy = CustomGenLayers.createAreaPvp(seed)

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
	    return when {
	    	ArenaManager.onEdge(x * 4, z * 4) -> inBetween
		    else -> biomeRegistry.d(BiomeRegistry.a(areaLazy.a(x, z)))
	    }
    }
}
