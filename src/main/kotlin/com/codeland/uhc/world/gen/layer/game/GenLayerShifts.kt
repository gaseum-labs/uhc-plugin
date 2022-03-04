package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.gen.UHCArea.UHCLayer
import kotlin.random.Random

class GenLayerShiftZZoom(seed: Long, val scale: Int) : UHCLayer(seed) {
	override fun sample(x: Int, z: Int): Int {
		val shiftZ = if (Util.mod(x, 2) == 0) {
			0
		} else {
			Random(Util.coordPack(Util.floorDiv(x, scale), 0, seed)).nextInt(2) * 2 - 1
		}

		return previous.sample(Util.floorDiv(x, scale), Util.floorDiv(z + shiftZ, scale))
	}
}

class GenLayerShiftX(seed: Long, val scale: Int) : UHCLayer(seed) {
	override fun sample(x: Int, z: Int): Int {
		val shiftX = if (Util.mod(z, 2) == 0) {
			0
		} else {
			Random(Util.coordPack(0, Util.floorDiv(z, scale), seed)).nextInt(2) * 2 - 1
		}

		return previous.sample(x + shiftX, z)
	}
}

class GenLayerCombiner(seed: Long) : UHCLayer(seed) {
	override fun sample(x: Int, z: Int): Int {
		val subX = Util.mod(Util.mod(x, 2) - 1, 2)
		val subZ = Util.mod(Util.mod(z, 2) - 1, 2)

		val up = Util.floorDiv(x - Util.mod(x, 2), 2)
		val left = Util.floorDiv(z - Util.mod(z, 2), 2)
		val down = Util.floorDiv(x + Util.mod(x, 2), 2)
		val right = Util.floorDiv(z + Util.mod(z, 2), 2)

		val ul = previous.sample(up, left)
		val ur = previous.sample(up, right)
		val dl = previous.sample(down, left)
		val dr = previous.sample(down, right)

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
			previous.sample(Util.floorDiv(x, 2), Util.floorDiv(z, 2))
		}
	}
}
