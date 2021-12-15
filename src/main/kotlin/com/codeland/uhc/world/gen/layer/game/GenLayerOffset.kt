package com.codeland.uhc.world.gen.layer.game

import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.AreaContextTransformed
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformerIdentity
import kotlin.random.Random

class GenLayerOffset(val seed: Long, val scale: Int) : AreaTransformer2, AreaTransformerIdentity {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		val random = Random(seed)
		val offX = random.nextInt(-scale, scale)
		val offZ = random.nextInt(-scale, scale)

		return area.a(x + offX, z + offZ)
	}
}
