package com.codeland.uhc.world.gen.layer.lobby

import net.minecraft.server.v1_16_R3.AreaTransformer4
import net.minecraft.server.v1_16_R3.WorldGenContext

class GenLayerOceanRiser : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (p1 == 164 || p2 == 164 || p3 == 164 || p4 == 164) {
			when (p5) {
				49 -> 46
				24 -> 0
				48 -> 45
				47 -> 44
				else -> p5
			}
		} else {
			p5
		}
	}
}
