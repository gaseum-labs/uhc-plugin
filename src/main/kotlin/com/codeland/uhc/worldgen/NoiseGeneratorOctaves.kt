package com.codeland.uhc.worldgen

import java.util.*

internal class NoiseGeneratorOctaves(seed: Random, octavesIn: Int) {
	/**
	 * Collection of noise generation functions. Output is combined to produce
	 * different octaves of noise.
	 */
	private val generatorCollection: Array<NoiseGeneratorImproved?>
	fun a(var0: Double, var2: Double, var4: Double, var6: Double, var8: Double,
		  var10: Boolean): Double {
		var var11 = 0.0
		var var12 = 1.0
		for (var13 in generatorCollection) {
			var11 += var13!!.a(a(var0 * var12), if (var10) -var13.b else a(var2 * var12), a(var4 * var12), var6 * var12,
				var8 * var12) / var12
			var12 /= 2.0
		}
		return var11
	}

	fun a(var0: Int): NoiseGeneratorImproved? {
		return generatorCollection[var0]
	}

	companion object {
		fun a(var0: Double): Double {
			return var0 - MathHelper.lFloor(var0 / 3.3554432E7 + 0.5) * 3.3554432E7
		}
	}

	init {
		generatorCollection = arrayOfNulls(octavesIn)
		for (i in 0 until octavesIn) {
			generatorCollection[i] = NoiseGeneratorImproved(seed)
		}
	}
}