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
				BiomeNo.SNOWY_TAIGA,
				BiomeNo.SNOWY_BEACH,
				BiomeNo.SNOWY_PLAINS,
				BiomeNo.SNOWY_SLOPES,
				-> BiomeNo.FROZEN_RIVER
				else -> BiomeNo.RIVER
			}
		} else {
			baseBiome
		}
	}
}
