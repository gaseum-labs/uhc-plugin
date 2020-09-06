package com.codeland.uhc.worldgen

import java.util.*

internal class NoiseGeneratorPerlin(p_i45470_1_: Random, private val levels: Int) {
	private val noiseLevels: Array<NoiseGeneratorSimplex?>
	fun func_202644_a(p_202644_1_: Double, p_202644_3_: Double, p_202644_5_: Int, p_202644_6_: Int, p_202644_7_: Double, p_202644_9_: Double, p_202644_11_: Double): DoubleArray {
		return func_202645_a(p_202644_1_, p_202644_3_, p_202644_5_, p_202644_6_, p_202644_7_, p_202644_9_, p_202644_11_, 0.5)
	}

	fun func_202645_a(p_202645_1_: Double, p_202645_3_: Double, p_202645_5_: Int, p_202645_6_: Int, p_202645_7_: Double, p_202645_9_: Double, p_202645_11_: Double, p_202645_13_: Double): DoubleArray {
		val adouble = DoubleArray(p_202645_5_ * p_202645_6_)
		var d0 = 1.0
		var d1 = 1.0
		for (i in 0 until levels) {
			noiseLevels[i]!!.add(adouble, p_202645_1_, p_202645_3_, p_202645_5_, p_202645_6_, p_202645_7_ * d1 * d0, p_202645_9_ * d1 * d0, 0.55 / d0)
			d1 *= p_202645_11_
			d0 *= p_202645_13_
		}
		return adouble
	}

	fun getValue(p_151601_1_: Double, p_151601_3_: Double): Double {
		var d0 = 0.0
		var d1 = 1.0
		for (i in 0 until levels) {
			d0 += noiseLevels[i]!!.getValue(p_151601_1_ * d1, p_151601_3_ * d1) / d1
			d1 /= 2.0
		}
		return d0
	}

	init {
		noiseLevels = arrayOfNulls(levels)
		for (i in 0 until levels) {
			noiseLevels[i] = NoiseGeneratorSimplex(p_i45470_1_)
		}
	}
}