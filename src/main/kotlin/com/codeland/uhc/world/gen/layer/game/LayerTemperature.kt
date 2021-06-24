package com.codeland.uhc.world.gen.layer.game

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1

class LayerTemperature : AreaTransformer1 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int): Int {
		return when (context.a(5)) {
			0 -> 0
			1 -> 1
			2 -> 1
			3 -> 1
			else -> 2
		}
	}
}
