package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1

class LayerUnique : AreaTransformer1 {
	override fun a(context: WorldGenContext, x: Int, z: Int): Int {
		return x.shl(16).or(z.and(0xffff))
	}
}


