package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.BiomeNo
import com.codeland.uhc.world.gen.ModifiedBiomesRegistry
import com.codeland.uhc.world.gen.UHCArea.UHCArea
import com.codeland.uhc.world.gen.layer.game.*
import net.minecraft.world.level.biome.*
import kotlin.math.abs

class BiomeSourceOverworldGame(
	val seed: Long,
	val centerBiomeNo: Int?,
	val endRadius: Int,
	val features: Boolean,
) : CheckerboardColumnBiomeSource(emptyList(), 1) {
	private val area = createAreaGame(seed)

	private val centerBiome = if (centerBiomeNo == null) null else biomeFromInt(centerBiomeNo)

	override fun getNoiseBiome(x: Int, y: Int, z: Int, niose: Climate.Sampler): Biome {
		/* center biome area */
		return if (centerBiome != null && inRange(x, z, endRadius)) {
			centerBiome

		} else {
			/* regular game area */
			biomeFromInt(area.sample(x, z))
		}
	}

	fun inRange(x: Int, z: Int, range: Int): Boolean {
		return abs(x) <= range / 4 && abs(z) <= range / 4
	}

	fun biomeFromInt(no: Int): Biome {
		return if (features) {
			ModifiedBiomesRegistry.featureBiomes[no]!!
		} else {
			BiomeNo.fromId(no)
		}
	}

	private fun createAreaGame(seed: Long): UHCArea {
		val area = UHCArea(LayerPerPlayer(seed))
			.addLayer(GenLayerShiftZZoom(seed, 3))
			.addLayer(GenLayerShiftX(seed, 3))
			.addLayer(GenLayerCombiner(seed))
			.addLayer(GenLayerShiftZZoom(seed, 2))
			.addLayer(GenLayerShiftX(seed, 2))
			.addLayer(GenLayerCombiner(seed))

		/* ----------------------------------------------------------------- */

		val riverArea = UHCArea(LayerNoise(seed))
			.addLayer(GenLayerShiftZZoom(seed, 3))
			.addLayer(GenLayerShiftX(seed, 3))
			.addLayer(GenLayerCombiner(seed))
			.addLayer(GenLayerShiftZZoom(seed, 2))
			.addLayer(GenLayerShiftX(seed, 2))
			.addLayer(GenLayerCombiner(seed))
			.addLayer(GenLayerEdge(seed, BiomeNo.RIVER))

		return area.merge(riverArea, GenLayerRiverApply(seed)).addLayer(GenLayerOffset(seed, 36))
	}
}
