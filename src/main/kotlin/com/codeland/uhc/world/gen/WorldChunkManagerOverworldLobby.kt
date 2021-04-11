package com.codeland.uhc.world.gen

import net.minecraft.server.v1_16_R3.*
import kotlin.math.abs
import kotlin.math.sqrt

class WorldChunkManagerOverworldLobby(
	var0: Long,
	var2: Boolean,
	var3: Boolean,
	private val var4: IRegistry<BiomeBase>,
	val biome1: ResourceKey<BiomeBase>,
	val biome2: ResourceKey<BiomeBase>,
	val centerBiome: ResourceKey<BiomeBase>,
	val oceanBiome: ResourceKey<BiomeBase>,
	val radius : Int
) : WorldChunkManagerOverworld(var0, var2, var3, var4) {
	fun squaredRadius(x: Int, z: Int, r: Int): Boolean {
		return abs(x) <= r / 4 && abs(z) <= r / 4
	}

	fun radius(x: Int, z: Int, r: Int): Boolean {
		return sqrt(x * x + z * z.toFloat()) <= r / 4
	}

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
	    return var4.d(when {
	    	radius(x, z, radius / 2) -> centerBiome
		    squaredRadius(x, z, radius) -> if (x > 0) biome1 else biome2
		    squaredRadius(x, z, radius + 16) -> Biomes.BEACH
		    else -> oceanBiome
	    })
    }
}
