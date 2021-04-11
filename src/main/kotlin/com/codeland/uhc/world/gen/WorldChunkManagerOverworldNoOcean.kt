package com.codeland.uhc.world.gen

import net.minecraft.server.v1_16_R3.*
import kotlin.math.abs

class WorldChunkManagerOverworldNoOcean(var0: Long, var2: Boolean, var3: Boolean, private val var4: IRegistry<BiomeBase>, val centerBiome: ResourceKey<BiomeBase>?) : WorldChunkManagerOverworld(var0, var2, var3, var4) {
	private val genLayerField = WorldChunkManagerOverworld::class.java.getDeclaredField("f")

	init {
		genLayerField.isAccessible = true
		genLayerField[this] = GenLayersNoOcean.createGenLayer(var0, var2, if (var3) 6 else 4, 4)
	}

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
        return if (centerBiome != null && abs(x) <= 8 && abs(z) <= 8)
        	var4.d(centerBiome)
        else
	        super.getBiome(x, y, z)
    }
}
