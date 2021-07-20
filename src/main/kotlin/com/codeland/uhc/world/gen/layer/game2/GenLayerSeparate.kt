package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer7

class GenLayerSeparate : AreaTransformer7 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		fun anyIs(value: Int): Boolean {
			return p1 == value || p2 == value || p3 == value || p4 == value
		}

		return when {
			(p5 == 0 && anyIs(1)) -> 2
			(p5 == 1 && anyIs(0)) -> 2
			else -> p5
		}
	}
}
