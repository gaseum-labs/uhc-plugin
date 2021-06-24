package com.codeland.uhc.world.gen.layer.game

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerEdge(val produce: Int) : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (p5 == 0) {
			if (p1 == 1 || p2 == 1 || p3 == 1 || p4 == 1) {
				produce
			} else {
				0
			}
		} else {
			0
		}
	}
}


