package com.codeland.uhc.world.gen.layer

import net.minecraft.server.v1_16_R3.AreaTransformer4
import net.minecraft.server.v1_16_R3.BiomeRegistry
import net.minecraft.server.v1_16_R3.WorldGenContext

class GenLayerShatteredIsland : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (context.a(10) == 0) {
			164
		} else {
			p5
		}
	}
}
