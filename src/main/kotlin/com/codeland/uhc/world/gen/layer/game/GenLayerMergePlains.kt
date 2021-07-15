package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerMergePlains : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		fun anyIs(t: Int): Boolean {
			return p1 == t || p2 == t || p3 == t || p4 == t
		}

		return if (anyIs(BiomeNo.PLAINS)) {
			BiomeNo.PLAINS
		} else {
			p5
		}
	}
}
