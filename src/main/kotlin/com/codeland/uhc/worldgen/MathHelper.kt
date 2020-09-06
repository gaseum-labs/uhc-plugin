package com.codeland.uhc.worldgen

object MathHelper {
	fun a(var0: Double, var2: Double, var4: Double, var6: Double, var8: Double, var10: Double): Double {
		return d(var2, d(var0, var4, var6), d(
			var0, var8, var10))
	}

	fun a(var0: Double, var2: Double, var4: Double, var6: Double,
		  var8: Double, var10: Double, var12: Double, var14: Double, var16: Double,
		  var18: Double, var20: Double): Double {
		// Is Mojang testing how many parameters a method can have, or what?
		return d(var4, a(var0, var2, var6, var8, var10, var12), a(
			var0, var2, var14, var16, var18, var20))
	}

	fun b(var0: Double, var2: Double, var4: Double): Double {
		if (var4 < 0.0) {
			return var0
		}
		return if (var4 > 1.0) {
			var2
		} else d(var4, var0, var2)
	}

	fun clampedLerp(lowerBnd: Double, upperBnd: Double, slide: Double): Double {
		val returnValue: Double
		returnValue = if (slide < 0.0) {
			lowerBnd
		} else {
			if (slide > 1.0) upperBnd else lowerBnd + (upperBnd - lowerBnd) * slide
		}
		return returnValue
	}

	fun d(var0: Double, var2: Double, var4: Double): Double {
		return var2 + var0 * (var4 - var2)
	}

	fun floor(var0: Double): Int {
		val `var` = var0.toInt()
		return if (var0 < `var`) `var` - 1 else `var`
	}

	fun j(var0: Double): Double {
		return var0 * var0 * var0 * (var0 * (var0 * 6.0 - 15.0) + 10.0)
	}

	fun lFloor(value: Double): Long {
		return if (value < 0) {
			// -3.5 becomes -3 when cast to long, but should be -4 for correct result
			value.toLong() - 1
		} else value.toLong()
	}

	fun sqrt(f: Float): Float {
		return Math.sqrt(f.toDouble()).toFloat()
	}
}