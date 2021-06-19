package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.server.v1_16_R3.*

class GenLayerRiverApply : AreaTransformer3, AreaTransformerIdentity {
	override fun a(p0: WorldGenContext, biomeArea: Area, riverArea: Area, x: Int, z: Int): Int {
		val baseBiome = biomeArea.a(a(x), b(z))

		return if (riverArea.a(a(x), b(z)) == 7) {
			when (baseBiome) {
				BiomeNo.SNOWY_TUNDRA -> BiomeNo.FROZEN_RIVER
				BiomeNo.SNOWY_TAIGA -> BiomeNo.FROZEN_RIVER
				BiomeNo.ICE_SPIKES -> BiomeNo.FROZEN_RIVER
				BiomeNo.SNOWY_TAIGA_MOUNTAINS -> BiomeNo.FROZEN_RIVER
				BiomeNo.SNOWY_MOUNTAINS -> BiomeNo.FROZEN_RIVER
				else -> BiomeNo.RIVER
			}
		} else {
			biomeArea.a(a(x), b(z))
		}
	}
}
