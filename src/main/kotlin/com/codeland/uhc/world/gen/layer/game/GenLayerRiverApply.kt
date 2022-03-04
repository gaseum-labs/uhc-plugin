package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.BiomeNo
import com.codeland.uhc.world.gen.UHCArea.UHCLayerMerge

class GenLayerRiverApply(seed: Long) : UHCLayerMerge(seed) {
	override fun sample(x: Int, z: Int): Int {
		val baseBiome = previous.sample(x, z)

		return if (otherPrevious.sample(x, z) == 7) {
			when (baseBiome) {
				BiomeNo.WARM_OCEAN -> BiomeNo.WARM_OCEAN
				BiomeNo.LUKEWARM_OCEAN -> BiomeNo.LUKEWARM_OCEAN
				BiomeNo.OCEAN -> BiomeNo.OCEAN
				BiomeNo.SNOWY_TUNDRA,
				BiomeNo.SNOWY_TAIGA,
				BiomeNo.ICE_SPIKES,
				BiomeNo.SNOWY_TAIGA_MOUNTAINS,
				BiomeNo.SNOWY_MOUNTAINS,
				-> BiomeNo.FROZEN_RIVER
				else -> BiomeNo.RIVER
			}
		} else {
			baseBiome
		}
	}
}
