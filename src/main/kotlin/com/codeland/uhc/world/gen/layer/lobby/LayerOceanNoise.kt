package com.codeland.uhc.world.gen.layer.lobby

import net.minecraft.server.v1_16_R3.*

class LayerOceanNoise : AreaTransformer1 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int): Int {
		return when (context.a(4)) {
			0 -> 46
			1 -> 0
			2 -> 45
			else -> 44
		}
	}
}
