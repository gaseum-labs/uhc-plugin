package com.codeland.uhc.world.gen.layer.lobby

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerOceanDeepener : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (context.a(2) == 0) {
			when (p5) {
				46 -> 49
				0 -> 24
				45 -> 48
				44 -> 47
				else -> p5
			}
		} else {
			p5
		}
	}
}
