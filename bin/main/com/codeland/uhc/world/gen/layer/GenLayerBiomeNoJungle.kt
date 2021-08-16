package com.codeland.uhc.world.gen.layer

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer5

class GenLayerBiomeNoJungle : AreaTransformer5 {
	companion object {
		private val a = intArrayOf(1, 2, 4, 3, 5, 6, 38, 39)
		private val b = intArrayOf(1, 3, 4, 6, 27, 29)
		private val c = intArrayOf(1, 3, 4, 5, 32)
		private val d = intArrayOf(12, 30)
	}

	override fun a(var0: WorldGenContext, incoming: Int): Int {
		val incomingBiome = incoming and (0x0f00.inv())

		return if (incomingBiome != 14) {
			when (incomingBiome) {
				1 -> a[var0.a(a.size)]
				2 -> b[var0.a(b.size)]
				3 -> c[var0.a(c.size)]
				4 -> d[var0.a(d.size)]
				else -> 14
			}
		} else {
			incomingBiome
		}
	}
}
