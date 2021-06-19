package com.codeland.uhc.world.gen.layer.game

import net.minecraft.server.v1_16_R3.*

class LayerNoise : AreaTransformer1 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int): Int {
		return context.a(2)
	}
}


