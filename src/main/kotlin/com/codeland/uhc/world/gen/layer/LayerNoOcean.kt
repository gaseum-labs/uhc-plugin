package com.codeland.uhc.world.gen.layer

import net.minecraft.server.v1_16_R3.*

class LayerNoOcean : AreaTransformer1 {
	override fun a(p0: WorldGenContext?, p1: Int, p2: Int): Int {
		return 1
	}
}
