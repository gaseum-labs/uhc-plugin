package com.codeland.uhc.world.gen.layer.lobby

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerShatteredIsland : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (context.a(10) == 0) {
			164
		} else {
			p2
		}
	}
}
