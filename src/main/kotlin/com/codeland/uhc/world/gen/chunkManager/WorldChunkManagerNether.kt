package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.CustomGenLayers
import com.codeland.uhc.world.gen.FeatureBiomes
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld

class WorldChunkManagerNether(
	val seed: Long,
	val biomeRegistry: IRegistry<BiomeBase>,
	val features: Boolean,
) : WorldChunkManagerOverworld(seed, false, false, biomeRegistry) {
	val area = CustomGenLayers.createAreaNether(seed)

	fun withFeatures(newFeatures: Boolean): WorldChunkManagerNether {
		return WorldChunkManagerNether(
			seed, biomeRegistry, newFeatures
		)
	}

	override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
		val biomeNo = area.a(x, z)

		return if (features) FeatureBiomes.biomes[biomeNo]!!
		else biomeRegistry.d(BiomeRegistry.a(biomeNo))
	}
}
