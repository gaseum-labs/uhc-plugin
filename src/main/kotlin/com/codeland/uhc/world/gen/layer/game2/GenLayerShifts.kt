package com.codeland.uhc.world.gen.layer.game2

import com.codeland.uhc.util.Util
import net.minecraft.world.level.newbiome.area.Area
import net.minecraft.world.level.newbiome.context.AreaContextTransformed
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformerIdentity
import kotlin.math.floor
import kotlin.random.Random

fun zoom(a: Int, scale: Int): Int {
	return floor(a / scale.toFloat()).toInt()
}

class GenLayerShiftZZoom(val scale: Int) : AreaTransformer2, AreaTransformerIdentity {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		val shiftZ = if (Util.mod(x, 2) == 0) {
			0
		} else {
			Random(zoom(x, scale).toLong()).nextInt(2) * 2 - 1
		}

		return area.a(zoom(x, scale), zoom(z + shiftZ, scale))
	}
}

class GenLayerShiftX(val scale: Int) : AreaTransformer2, AreaTransformerIdentity {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		val shiftX = if (Util.mod(z, 2) == 0) {
			0
		} else {
			Random(zoom(z, scale).toLong()).nextInt(2) * 2 - 1
		}

		return area.a(x + shiftX, z)
	}
}

class GenLayerCombiner() : AreaTransformer2, AreaTransformerIdentity {
	override fun a(p0: AreaContextTransformed<*>, area: Area, x: Int, z: Int): Int {
		val baseX = zoom(x, 2)
		val baseZ = zoom(z, 2)

		val subX = Util.mod(Util.mod(x, 2) - 1, 2)
		val subZ = Util.mod(Util.mod(z, 2) - 1, 2)

		val up    = zoom(x - Util.mod(x, 2), 2)
		val left  = zoom(z - Util.mod(z, 2), 2)
		val down  = zoom(x + Util.mod(x, 2), 2)
		val right = zoom(z + Util.mod(z, 2), 2)

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
			area.a(zoom(x, 2), zoom(z, 2))
		}
	}
}
