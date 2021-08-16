package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer7
import kotlin.random.Random

class GenLayerHole : AreaTransformer7 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (p1 != p5 && p2 != p5 && p3 == p5 && p4 == p5) {
			Random(p5).nextInt(0, 2)
		} else if (p1 == p5 && p2 == p5 && p3 != p5 && p4 != p5 && context.a(2) == 0) {
			Random(p5).nextInt(3, 5)
		} else {
			2
		}
	}
}
