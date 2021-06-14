package com.codeland.uhc.world.gen

import net.minecraft.server.v1_16_R3.*
import kotlin.math.*

class WorldChunkManagerOverworldLobby(
	seed: Long,
	private val var4: IRegistry<BiomeBase>,
	val biomes: List<ResourceKey<BiomeBase>>,
	squareRadius : Int
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val radius = diagonalToSquare(squareRadius)
	private val genLayerField = WorldChunkManagerOverworld::class.java.getDeclaredField("f")

	init {
		genLayerField.isAccessible = true
		genLayerField[this] = CustomGenLayers.createGenLayerLobby(seed)
	}

	fun radius(x: Int, z: Int, r: Float): Boolean {
		return sqrt(x * x + z * z.toFloat()) <= r / 4
	}

	fun angleIndex(x: Int, z: Int, size: Int): Int {
		return ((atan2(x.toFloat(), z.toFloat()) + PI) / (2 * PI) * size).toInt().coerceAtLeast(0).coerceAtMost(size - 1)
	}

	fun diagonalToSquare(radius: Int): Float {
		return 2.0F / sqrt(2.0F) * radius
	}

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
	    return when {
	    	radius(x, z, radius) -> var4.d(biomes[angleIndex(x, z, biomes.size)])
		    radius(x, z, radius + 16) -> var4.d(Biomes.BEACH)
		    else -> super.getBiome(x, y, z)
	    }
    }
}
