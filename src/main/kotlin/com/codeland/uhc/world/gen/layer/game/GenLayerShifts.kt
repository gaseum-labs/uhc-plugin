package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.util.Util
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.AreaContextTransformed
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformerIdentity
import kotlin.random.Random

class GenLayerShiftZZoom(val seed: Long, val scale: Int) : AreaTransformer2, AreaTransformerIdentity {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		val shiftZ = if (Util.mod(x, 2) == 0) {
			0
		} else {
			Random(Util.coordPack(Util.floorDiv(x, scale), 0, seed)).nextInt(2) * 2 - 1
		}

		return area.a(Util.floorDiv(x, scale), Util.floorDiv(z + shiftZ, scale))
	}
}

class GenLayerShiftX(val seed: Long, val scale: Int) : AreaTransformer2, AreaTransformerIdentity {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		val shiftX = if (Util.mod(z, 2) == 0) {
			0
		} else {
			Random(Util.coordPack(0, Util.floorDiv(z, scale), seed)).nextInt(2) * 2 - 1
		}

		return area.a(x + shiftX, z)
	}
}

class GenLayerCombiner : AreaTransformer2, AreaTransformerIdentity {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		val subX = Util.mod(Util.mod(x, 2) - 1, 2)
		val subZ = Util.mod(Util.mod(z, 2) - 1, 2)

		val up = Util.floorDiv(x - Util.mod(x, 2), 2)
		val left = Util.floorDiv(z - Util.mod(z, 2), 2)
		val down = Util.floorDiv(x + Util.mod(x, 2), 2)
		val right = Util.floorDiv(z + Util.mod(z, 2), 2)

		val ul = area.a(up, left)
		val ur = area.a(up, right)
		val dl = area.a(down, left)
		val dr = area.a(down, right)

		/* [X] [*]
		 *
		 * [*] [X]
		 */
		return if (((subX == 0 && subZ == 0) || (subX == 1 && subZ == 1)) && ur == dl) {
			ur

			/* [*] [X]
			 *
			 * [X] [*]
			 */
		} else if (((subX == 1 && subZ == 0) || (subX == 0 && subZ == 1)) && ul == dr) {
			ul

		} else {
			area.a(Util.floorDiv(x, 2), Util.floorDiv(z, 2))
		}
	}
}
