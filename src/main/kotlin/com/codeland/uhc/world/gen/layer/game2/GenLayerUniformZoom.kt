package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.AreaContextTransformed
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2

class GenLayerUniformZoom : AreaTransformer2 {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		return area.a(a(x), b(z))
	}

	override fun a(x: Int): Int {
		return x.shr(1)
	}

	override fun b(z: Int): Int {
		return z.shr(1)
	}
}
