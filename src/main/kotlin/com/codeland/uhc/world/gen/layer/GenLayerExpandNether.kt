package com.codeland.uhc.world.gen.layer

import net.minecraft.server.v1_16_R3.AreaTransformer4
import net.minecraft.server.v1_16_R3.WorldGenContext

class GenLayerExpandNether : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (p5 == 0) {
			/* half way either wastes or delta */
			if (context.a(2) == 0) 8 else 173

		/* pass through other biomes*/
		} else {
			p5
		}
	}
}
