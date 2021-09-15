package com.codeland.uhc.world.gen.layer.game2

import com.codeland.uhc.util.Util
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.AreaContextTransformed
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
import kotlin.random.Random

class GenLayerUniformZoom3 : AreaTransformer2 {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		val baseX = a(x)
		val baseZ = b(z)

		val walls =  Random(baseX.toLong().shl(32).or(baseZ.toLong().and(0x0000ffff))).nextInt(3 * 3 * 3 * 3)

		val sx = Util.mod(x, 3)
		val sz = Util.mod(z, 3)

		return area.a(baseX + if (sx == 0 && sz == walls % 3) {
			-1
		} else if (sx == 2 && sz == (walls / 3) % 3) {
			1
		} else {
			0
		}, baseZ + if (sz == 0 && sx == (walls / (3 * 3)) % 3) {
			-1
		} else if (sz == 2 && sx == (walls / (3 * 3 * 3)) % 3) {
			1
		} else {
			0
		})
	}

	override fun a(x: Int): Int {
		return x / 3
	}

	override fun b(z: Int): Int {
		return z / 3
	}
}
