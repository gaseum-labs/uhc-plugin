package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerSplit : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		val (region, special) = Region.unpack(p5)

		return when {
			special -> region.special
			context.a(3) == 0 -> region.internal
			else -> region.main
		}
	}
}
