package com.codeland.uhc.world.gen.biomeSource

import com.codeland.uhc.world.gen.BiomeNo
import com.codeland.uhc.world.gen.CustomCarvers
import com.codeland.uhc.world.gen.UHCArea.UHCArea
import com.codeland.uhc.world.gen.layer.game.*
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.world.level.biome.*
import kotlin.math.abs

class BiomeSourceGame(
	val seed: Long,
	val centerBiomeNo: Int?,
	val endRadius: Int,
	val features: Boolean,
	val area: UHCArea,
) : CheckerboardColumnBiomeSource(BiomeNo.biomeHolderSet, 1) {
	private val centerBiome = if (centerBiomeNo == null) null else biomeFromInt(centerBiomeNo)

	override fun getNoiseBiome(x: Int, y: Int, z: Int, noise: Climate.Sampler): Holder<Biome> {
		//println(noise.sample(x, y, z).humidity)

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

	fun biomeFromInt(no: Int): Holder<Biome> {
		return if (features) {
			BiomeNo.featureBiomes[no]!!
		} else {
			BiomeNo.fromId(no)
		}
	}

	companion object {
		fun createFeaturesPair(
			seed: Long,
			centerBiomeId: Int?,
			endRadius: Int,
		): Pair<BiomeSourceGame, BiomeSourceGame> {
			val area = createAreaGame(seed)

			return Pair(
				BiomeSourceGame(seed, centerBiomeId, endRadius, false, area),
				BiomeSourceGame(seed, centerBiomeId, endRadius, true, area)
			)
		}

		fun createAreaGame(seed: Long): UHCArea {
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
}
