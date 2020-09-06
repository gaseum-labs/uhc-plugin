package com.codeland.uhc.worldgen

import java.util.*

internal class NoiseGeneratorSimplex(p_i45471_1_: Random) {
	private val p = IntArray(512)
	var xo: Double
	var yo: Double
	var zo: Double
	fun add(p_151606_1_: DoubleArray, p_151606_2_: Double, p_151606_4_: Double, p_151606_6_: Int, p_151606_7_: Int,
			p_151606_8_: Double, p_151606_10_: Double, p_151606_12_: Double) {
		var i = 0
		for (j in 0 until p_151606_7_) {
			val d0 = (p_151606_4_ + j) * p_151606_10_ + yo
			for (k in 0 until p_151606_6_) {
				val d1 = (p_151606_2_ + k) * p_151606_8_ + xo
				val d5 = (d1 + d0) * F2
				val l = fastFloor(d1 + d5)
				val i1 = fastFloor(d0 + d5)
				val d6 = (l + i1) * G2
				val d7 = l - d6
				val d8 = i1 - d6
				val d9 = d1 - d7
				val d10 = d0 - d8
				var j1: Int
				var k1: Int
				if (d9 > d10) {
					j1 = 1
					k1 = 0
				} else {
					j1 = 0
					k1 = 1
				}
				val d11 = d9 - j1 + G2
				val d12 = d10 - k1 + G2
				val d13 = d9 - 1.0 + 2.0 * G2
				val d14 = d10 - 1.0 + 2.0 * G2
				val l1 = l and 255
				val i2 = i1 and 255
				val j2 = p[l1 + p[i2]] % 12
				val k2 = p[l1 + j1 + p[i2 + k1]] % 12
				val l2 = p[l1 + 1 + p[i2 + 1]] % 12
				var d15 = 0.5 - d9 * d9 - d10 * d10
				var d2: Double
				if (d15 < 0.0) {
					d2 = 0.0
				} else {
					d15 = d15 * d15
					d2 = d15 * d15 * dot(grad3[j2], d9, d10)
				}
				var d16 = 0.5 - d11 * d11 - d12 * d12
				var d3: Double
				if (d16 < 0.0) {
					d3 = 0.0
				} else {
					d16 = d16 * d16
					d3 = d16 * d16 * dot(grad3[k2], d11, d12)
				}
				var d17 = 0.5 - d13 * d13 - d14 * d14
				var d4: Double
				if (d17 < 0.0) {
					d4 = 0.0
				} else {
					d17 = d17 * d17
					d4 = d17 * d17 * dot(grad3[l2], d13, d14)
				}
				val i3 = i++
				p_151606_1_[i3] += 70.0 * (d2 + d3 + d4) * p_151606_12_
			}
		}
	}

	fun getValue(p_151605_1_: Double, p_151605_3_: Double): Double {
		val d3 = 0.5 * (SQRT_3 - 1.0)
		val d4 = (p_151605_1_ + p_151605_3_) * d3
		val i = fastFloor(p_151605_1_ + d4)
		val j = fastFloor(p_151605_3_ + d4)
		val d5 = (3.0 - SQRT_3) / 6.0
		val d6 = (i + j) * d5
		val d7 = i - d6
		val d8 = j - d6
		val d9 = p_151605_1_ - d7
		val d10 = p_151605_3_ - d8
		val k: Int
		val l: Int
		if (d9 > d10) {
			k = 1
			l = 0
		} else {
			k = 0
			l = 1
		}
		val d11 = d9 - k + d5
		val d12 = d10 - l + d5
		val d13 = d9 - 1.0 + 2.0 * d5
		val d14 = d10 - 1.0 + 2.0 * d5
		val i1 = i and 255
		val j1 = j and 255
		val k1 = p[i1 + p[j1]] % 12
		val l1 = p[i1 + k + p[j1 + l]] % 12
		val i2 = p[i1 + 1 + p[j1 + 1]] % 12
		var d15 = 0.5 - d9 * d9 - d10 * d10
		val d0: Double
		if (d15 < 0.0) {
			d0 = 0.0
		} else {
			d15 = d15 * d15
			d0 = d15 * d15 * dot(grad3[k1], d9, d10)
		}
		var d16 = 0.5 - d11 * d11 - d12 * d12
		val d1: Double
		if (d16 < 0.0) {
			d1 = 0.0
		} else {
			d16 = d16 * d16
			d1 = d16 * d16 * dot(grad3[l1], d11, d12)
		}
		var d17 = 0.5 - d13 * d13 - d14 * d14
		val d2: Double
		if (d17 < 0.0) {
			d2 = 0.0
		} else {
			d17 = d17 * d17
			d2 = d17 * d17 * dot(grad3[i2], d13, d14)
		}
		return 70.0 * (d0 + d1 + d2)
	}

	companion object {
		private val grad3 = arrayOf(intArrayOf(1, 1, 0), intArrayOf(-1, 1, 0), intArrayOf(1, -1, 0), intArrayOf(-1, -1, 0), intArrayOf(1, 0, 1), intArrayOf(-1, 0, 1), intArrayOf(1, 0, -1), intArrayOf(-1, 0, -1), intArrayOf(0, 1, 1), intArrayOf(0, -1, 1), intArrayOf(0, 1, -1), intArrayOf(0, -1, -1))
		val SQRT_3 = Math.sqrt(3.0)
		private val F2 = 0.5 * (SQRT_3 - 1.0)
		private val G2 = (3.0 - SQRT_3) / 6.0
		private fun dot(p_151604_0_: IntArray, p_151604_1_: Double, p_151604_3_: Double): Double {
			return p_151604_0_[0] * p_151604_1_ + p_151604_0_[1] * p_151604_3_
		}

		private fun fastFloor(value: Double): Int {
			return if (value > 0.0) value.toInt() else value.toInt() - 1
		}
	}

	init {
		xo = p_i45471_1_.nextDouble() * 256.0
		yo = p_i45471_1_.nextDouble() * 256.0
		zo = p_i45471_1_.nextDouble() * 256.0
		var i = 0
		while (i < 256) {
			p[i] = i++
		}
		for (l in 0..255) {
			val j = p_i45471_1_.nextInt(256 - l) + l
			val k = p[l]
			p[l] = p[j]
			p[j] = k
			p[l + 256] = p[l]
		}
	}
}