package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.ModifiedBiomesRegistry
import com.codeland.uhc.world.gen.layer.game.*
import com.codeland.uhc.world.gen.layer.nether.LayerNetherBiome
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.WorldGenContextArea

class WorldChunkManagerNether(
	val seed: Long,
	val biomeRegistry: IRegistry<BiomeBase>,
	val features: Boolean,
) : WorldChunkManagerOverworld(seed, false, false, biomeRegistry) {
	val area = createAreaNether(seed)

	fun withFeatures(newFeatures: Boolean): WorldChunkManagerNether {
		return WorldChunkManagerNether(
			seed, biomeRegistry, newFeatures
		)
	}

	override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
		val biomeNo = area.a(x, z)

		return if (features) ModifiedBiomesRegistry.featureBiomes[biomeNo]!!
		else biomeRegistry.d(BiomeRegistry.a(biomeNo))
	}

	fun createAreaNether(seed: Long): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerNetherBiome(seed).a(noise(4500L))  /* 4X */

		baseLayer = GenLayerShiftZZoom(seed, 3).a(noise(1L), baseLayer)   /* 3X */
		baseLayer = GenLayerShiftX(seed, 3).a(noise(1L), baseLayer)
		baseLayer = GenLayerCombiner().a(noise(1L), baseLayer)   /* 2X */
		baseLayer = GenLayerShiftZZoom(seed, 2).a(noise(1L), baseLayer)   /* 2X */
		baseLayer = GenLayerShiftX(seed, 2).a(noise(1L), baseLayer)
		baseLayer = GenLayerCombiner().a(noise(1L), baseLayer)   /* 2X */

		return baseLayer.make()
	}
}
