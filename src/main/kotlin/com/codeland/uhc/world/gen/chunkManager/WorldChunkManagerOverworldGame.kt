package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.BiomeNo
import com.codeland.uhc.world.gen.ModifiedBiomesRegistry
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
	val endRadius: Int,
	val features: Boolean,
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val areaLazy = createAreaGame(seed)

	private val centerBiome = if (centerBiomeNo == null) null else biomeFromInt(centerBiomeNo)

	fun withFeatures(newFeatures: Boolean): WorldChunkManagerOverworldGame {
		return WorldChunkManagerOverworldGame(
			seed, var4, centerBiomeNo, endRadius, newFeatures
		)
	}

	fun inRange(x: Int, z: Int, range: Int): Boolean {
		return abs(x) <= range / 4 && abs(z) <= range / 4
	}

	fun biomeFromInt(no: Int): BiomeBase {
		return if (features) {
			ModifiedBiomesRegistry.featureBiomes[no]!!
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

	private fun createAreaGame(seed: Long): Area {
		val area = { l: Long -> WorldGenContextArea(25, seed, l) }

		var baseLayer = LayerPerPlayer(seed).a(area(0L))  /* 4X */

		baseLayer = GenLayerShiftZZoom(seed, 3).a(area(0L), baseLayer)   /* 3X */
		baseLayer = GenLayerShiftX(seed, 3).a(area(0L), baseLayer)
		baseLayer = GenLayerCombiner().a(area(0L), baseLayer)   /* 2X */
		baseLayer = GenLayerShiftZZoom(seed, 2).a(area(0L), baseLayer)   /* 2X */
		baseLayer = GenLayerShiftX(seed, 2).a(area(0L), baseLayer)
		baseLayer = GenLayerCombiner().a(area(0L), baseLayer)   /* 2X */

		/* ----------------------------------------------------------------- */

		var riverLayer = LayerNoise().a(area(0L))                        /* 4X */

		riverLayer = GenLayerShiftZZoom(seed, 3).a(area(0L), riverLayer) /* 3X */
		riverLayer = GenLayerShiftX(seed, 3).a(area(0L), riverLayer)
		riverLayer = GenLayerCombiner().a(area(0L), riverLayer) /* 2X */
		riverLayer = GenLayerShiftZZoom(seed, 2).a(area(0L), riverLayer) /* 2X */
		riverLayer = GenLayerShiftX(seed, 2).a(area(0L), riverLayer)
		riverLayer = GenLayerCombiner().a(area(0L), riverLayer) /* 2X */
		riverLayer = GenLayerCombiner().a(area(0L), riverLayer) /* 2X (extra) */

		riverLayer = GenLayerEdge(BiomeNo.RIVER).a(area(0L), riverLayer)

		baseLayer = GenLayerRiverApply().a(area(0L), baseLayer, riverLayer)
		baseLayer = GenLayerOffset(seed, 36).a(area(0L), baseLayer)

		return baseLayer.make()
	}
}
