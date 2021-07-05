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

	private fun inRange(sx: Int, sz: Int, size: Int): Boolean {
		val border = (stride - size) / 2
		return sx > border && sx < stride - border && sz > border && sz < stride - border
	}

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
	    val cx = floor(x.toFloat() / (stride / 4)).toInt() + Short.MAX_VALUE / 2
	    val cz = floor(z.toFloat() / (stride / 4)).toInt() + Short.MAX_VALUE / 2

	    val sx = Util.mod(x, stride / 4) * 4
	    val sz = Util.mod(z, stride / 4) * 4

	    return when {
	    	inRange(sx, sz, PvpGameManager.BORDER) -> biomeRegistry.d(BiomeRegistry.a(areaLazy.a(x, z)))
		    else -> inBetween
	    }
    }
}
