package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer7

class GenLayerCohere : AreaTransformer7 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		fun expand(to: Int, avoid: Int): Int? {
			return when {
				p1 == to && p3 != avoid -> to
				p2 == to && p4 != avoid -> to
				p3 == to && p1 != avoid -> to
				p4 == to && p2 != avoid -> to
				else -> null
			}
		}

		return if (p5 == 2) {
			if (context.a(2) == 0) {
				expand(0, 1) ?: expand(1, 0) ?: expand(3, 9999) ?: p5
			} else {
				expand(1, 0) ?: expand(0, 1) ?: expand(3, 9999) ?: p5
			}
		} else {
			p5
		}
	}
}
