package com.codeland.uhc.world.gen.layer

import net.minecraft.server.v1_16_R3.*

class LayerOceanNoise : AreaTransformer1 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int): Int {
		return when (context.a(7)) {
			0 -> 0
			1 -> 24
			2 -> 44
			3 -> 45
			4 -> 46
			5 -> 48
			else -> 49
		}
	}
}
