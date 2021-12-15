package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.layer.game.*
import com.codeland.uhc.world.gen.layer.pvp.LayerPvp
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.WorldGenContextArea

class WorldChunkManagerOverworldPvp(
	val seed: Long,
	private val biomeRegistry: IRegistry<BiomeBase>,
) : WorldChunkManagerOverworld(seed, false, false, biomeRegistry) {
	private val areaLazy = createAreaPvp(seed)

	override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
		return biomeRegistry.d(BiomeRegistry.a(areaLazy.a(x, z)))
	}

	private fun createAreaPvp(seed: Long): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerPvp().a(noise(1000L))

		baseLayer = GenLayerShiftZZoom(seed, 2).a(noise(100L), baseLayer)
		baseLayer = GenLayerShiftX(seed, 2).a(noise(100L), baseLayer)
		baseLayer = GenLayerCombiner().a(noise(100L), baseLayer)

		baseLayer = GenLayerShiftZZoom(seed, 2).a(noise(100L), baseLayer)
		baseLayer = GenLayerShiftX(seed, 2).a(noise(100L), baseLayer)
		baseLayer = GenLayerCombiner().a(noise(100L), baseLayer)

		return baseLayer.make()
	}
}
