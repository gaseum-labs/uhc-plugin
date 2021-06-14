package com.codeland.uhc.world.gen.layer

import net.minecraft.server.v1_16_R3.AreaTransformer1
import net.minecraft.server.v1_16_R3.WorldGenContext

class LayerNetherBase : AreaTransformer1 {
	override fun a(context: WorldGenContext, var1: Int, var2: Int): Int {
		return when (context.a(8)) {
			0 -> 170  /* SOUL SAND */
			1 -> 171  /* CRIMSON */
			2 -> 172  /* WARPED */
			3 -> 0    /* placeholder */
			else -> 8 /* NETHER WASTES */
		}
	}
}