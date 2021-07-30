package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1

class LayerNoise : AreaTransformer1 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int): Int {
		return context.a(2)
	}
}


