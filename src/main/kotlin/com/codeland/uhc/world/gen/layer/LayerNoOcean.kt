package com.codeland.uhc.world.gen.layer

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1

class LayerNoOcean : AreaTransformer1 {
	override fun a(p0: WorldGenContext?, p1: Int, p2: Int): Int {
		return 1
	}
}
