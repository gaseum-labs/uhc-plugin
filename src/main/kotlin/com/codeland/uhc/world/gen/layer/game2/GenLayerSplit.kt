package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerSplit: AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (context.a(3) == 0)
			Region.values()[p5].internal
		else
			Region.values()[p5].main
	}
}
