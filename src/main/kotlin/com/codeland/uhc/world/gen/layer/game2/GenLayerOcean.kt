package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.AreaContextTransformed
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformerIdentity
import kotlin.math.abs
import kotlin.math.pow

class GenLayerOcean(borderRadius: Int, zoomsAfter: Int)  : AreaTransformer2, AreaTransformerIdentity {
	val limit = borderRadius / (4 * 2.0f.pow(zoomsAfter).toInt())

	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		return if (abs(x) > limit || abs(z) > limit) {
			Region.OCEAN.ordinal
		} else {
			area.a(x, z)
		}
	}
}
