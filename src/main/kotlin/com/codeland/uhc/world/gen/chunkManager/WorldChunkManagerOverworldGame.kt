package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.*
import com.codeland.uhc.world.gen.layer.game.*
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.WorldGenContextArea
import kotlin.math.abs

class WorldChunkManagerOverworldGame(
	val seed: Long,
	private val var4: IRegistry<BiomeBase>,
	val centerBiomeNo: Int?,
	val startRadius: Int,
	val endRadius: Int,
	val features: Boolean,
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val areaLazy = createAreaGame(seed, startRadius)

	private val centerBiome = if (centerBiomeNo == null) null else biomeFromInt(centerBiomeNo)

	fun withFeatures(newFeatures: Boolean): WorldChunkManagerOverworldGame {
		return WorldChunkManagerOverworldGame(
			seed, var4, centerBiomeNo, startRadius, endRadius, newFeatures
		)
	}

	fun inRange(x: Int, z: Int, range: Int): Boolean {
		return abs(x) <= range / 4 && abs(z) <= range / 4
	}

	fun biomeFromInt(no: Int): BiomeBase {
		return if (features) {
			FeatureBiomes.biomes[no]!!
		} else {
			var4.d(BiomeRegistry.a(no))
		}
	}

	override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
		/* center biome area */
		return if (centerBiome != null && inRange(x, z, endRadius)) {
			centerBiome

			/* regular game area */
		} else {
			biomeFromInt(areaLazy.a(x, z))
		}
	}

	private fun createAreaGame(seed: Long, borderRadius: Int): Area {
		val noise = { s: Long -> WorldGenContextArea(25, seed, s) }

		var baseLayer = LayerTemperature(seed, borderRadius / 96).a(noise(0L))  /* 4X */

		baseLayer = GenLayerShiftZZoom(seed, 3).a(noise(1L), baseLayer)   /* 3X */
		baseLayer = GenLayerShiftX(seed, 3).a(noise(1L), baseLayer)
		baseLayer = GenLayerCombiner().a(noise(1L), baseLayer)   /* 2X */

		baseLayer = GenLayerBorder().a(noise(7070L), baseLayer)
		baseLayer = GenLayerSplit().a(noise(7071L), baseLayer)

		baseLayer = GenLayerShiftZZoom(seed, 2).a(noise(1L), baseLayer)   /* 2X */
		baseLayer = GenLayerShiftX(seed, 2).a(noise(1L), baseLayer)
		baseLayer = GenLayerCombiner().a(noise(1L), baseLayer)   /* 2X */

		/* -------------------------------------------------------------------- */

		var riverLayer = LayerNoise().a(noise(0L))                        /* 4X */

		riverLayer = GenLayerShiftZZoom(seed, 3).a(noise(1L), riverLayer) /* 3X */
		riverLayer = GenLayerShiftX(seed, 3).a(noise(1L), riverLayer)
		riverLayer = GenLayerCombiner().a(noise(1L), riverLayer) /* 2X */
		riverLayer = GenLayerShiftZZoom(seed, 2).a(noise(1L), riverLayer) /* 2X */
		riverLayer = GenLayerShiftX(seed, 2).a(noise(1L), riverLayer)
		riverLayer = GenLayerCombiner().a(noise(1L), riverLayer) /* 2X */
		riverLayer = GenLayerCombiner().a(noise(1L), riverLayer) /* 2X (extra) */

		riverLayer = GenLayerEdge(BiomeNo.RIVER).a(noise(9090L), riverLayer)

		baseLayer = GenLayerRiverApply().a(noise(9091L), baseLayer, riverLayer)

		return baseLayer.make()
	}
}
