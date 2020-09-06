package com.codeland.uhc.worldgen

import java.util.*
import kotlin.experimental.and

class NoiseGeneratorImproved(var0: Random) {
	private val d: ByteArray
	val a: Double
	val b: Double
	val c: Double
	fun a(var0: Double, var2: Double, var4: Double, var6: Double, var8: Double): Double {
		val var9 = var0 + a
		val var10 = var2 + b
		val var11 = var4 + c
		val var12 = MathHelper.floor(var9)
		val var13 = MathHelper.floor(var10)
		val var14 = MathHelper.floor(var11)
		val var15 = var9 - var12
		val var16 = var10 - var13
		val var17 = var11 - var14
		val var18 = MathHelper.j(var15)
		val var19 = MathHelper.j(var16)
		val var20 = MathHelper.j(var17)
		val var22: Double
		var22 = if (var6 != 0.0) {
			val var21 = Math.min(var8, var16)
			MathHelper.floor(var21 / var6) * var6
		} else {
			0.0
		}
		return this.a(var12, var13, var14, var15, var16 - var22, var17, var18, var19, var20)
	}

	private fun a(var0: Int): Int {
		return (d[var0 and 0xFF] and 0xFF.toByte()).toInt()
	}

	fun a(var0: Int, var1: Int, var2: Int, var3: Double, var5: Double,
		  var7: Double, var9: Double, var11: Double, var13: Double): Double {
		val var14 = this.a(var0) + var1
		val var15 = this.a(var14) + var2
		val var16 = this.a(var14 + 1) + var2
		val var17 = this.a(var0 + 1) + var1
		val var18 = this.a(var17) + var2
		val var19 = this.a(var17 + 1) + var2
		val var20 = a(this.a(var15), var3, var5, var7)
		val var21 = a(this.a(var18), var3 - 1.0, var5, var7)
		val var22 = a(this.a(var16), var3, var5 - 1.0, var7)
		val var23 = a(this.a(var19), var3 - 1.0, var5 - 1.0, var7)
		val var24 = a(this.a(var15 + 1), var3, var5, var7 - 1.0)
		val var25 = a(this.a(var18 + 1), var3 - 1.0, var5, var7 - 1.0)
		val var26 = a(this.a(var16 + 1), var3, var5 - 1.0, var7 - 1.0)
		val var27 = a(this.a(var19 + 1), var3 - 1.0, var5 - 1.0, var7 - 1.0)
		return MathHelper.a(var9, var11, var13, var20, var21, var22, var23, var24, var25, var26, var27)
	}

	companion object {
		private fun a(var0: Int, var1: Double, var3: Double, var5: Double): Double {
			val var6 = var0 and 0xF
			return NoiseGenerator3Handler.a(NoiseGenerator3Handler.a[var6], var1, var3, var5)
		}
	}

	init {
		a = var0.nextDouble() * 256.0
		b = var0.nextDouble() * 256.0
		c = var0.nextDouble() * 256.0
		d = ByteArray(256)
		for (`var` in 0..255) {
			d[`var`] = `var`.toByte()
		}
		for (`var` in 0..255) {
			val var2 = var0.nextInt(256 - `var`)
			val var3 = d[`var`]
			d[`var`] = d[`var` + var2]
			d[`var` + var2] = var3
		}
	}
}