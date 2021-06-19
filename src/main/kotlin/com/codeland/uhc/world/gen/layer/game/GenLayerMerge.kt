package com.codeland.uhc.world.gen.layer.game

import net.minecraft.server.v1_16_R3.AreaTransformer4
import net.minecraft.server.v1_16_R3.WorldGenContext

class GenLayerMerge : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		fun anyIs(t: Int): Boolean {
			return p1 == t || p2 == t || p3 == t || p4 == t
		}

		return when (p5) {
			0 -> when {
				anyIs(2) -> 1
				else -> 0
			}
			1 -> 1
			else -> when {
				anyIs(0) -> 1
				else -> 3
			}
		}
	}
}
