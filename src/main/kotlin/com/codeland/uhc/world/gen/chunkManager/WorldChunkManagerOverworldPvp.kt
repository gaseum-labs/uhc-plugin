package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.world.gen.BiomeNo
import com.codeland.uhc.world.gen.layer.pvp.LayerPvp
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.WorldGenContextArea
import net.minecraft.world.level.newbiome.layer.GenLayerZoom

class WorldChunkManagerOverworldPvp(
	val seed: Long,
	private val biomeRegistry: IRegistry<BiomeBase>,
) : WorldChunkManagerOverworld(seed, false, false, biomeRegistry) {
	private val inBetween: BiomeBase = biomeRegistry.d(BiomeRegistry.a(BiomeNo.BEACH))

	private val areaLazy = createAreaPvp(seed)

	override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
		return when {
			ArenaManager.onEdge(x * 4, z * 4) -> inBetween
			else -> biomeRegistry.d(BiomeRegistry.a(areaLazy.a(x, z)))
		}
	}

	private fun createAreaPvp(seed: Long): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerPvp().a(noise(1000L))
		baseLayer = GenLayerZoom.a.a(noise(1001L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1002L), baseLayer)
		baseLayer = GenLayerZoom.a.a(noise(1003L), baseLayer)
		baseLayer = GenLayerZoom.ar.a(noise(1004L), baseLayer)

		return baseLayer.make()
	}
}
