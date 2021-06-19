package com.codeland.uhc.world.gen.layer.game

import net.minecraft.server.v1_16_R3.*

class GenLayerExpander(val expand: Int) : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		val side = context.a(2) == 1

		return if (
			(side && (p1 == expand || p3 == expand)) ||
			(!side && (p2 == expand || p4 == expand))
		) {
			expand
		} else {
			p5
		}
	}
}


