package com.codeland.uhc.worldgen

internal object NoiseGenerator3Handler {
	var a: Array<IntArray?>
	fun a(var0: IntArray?, var1: Double, var3: Double, var5: Double): Double {
		return var0!![0] * var1 + var0[1] * var3 + var0[2] * var5
	}

	init {
		a = arrayOf(intArrayOf(1, 1, 0), intArrayOf(-1, 1, 0), intArrayOf(1, -1, 0), intArrayOf(-1, -1, 0), intArrayOf(1, 0, 1), intArrayOf(-1, 0, 1), intArrayOf(1, 0, -1), intArrayOf(-1, 0, -1), intArrayOf(0, 1, 1), intArrayOf(0, -1, 1), intArrayOf(0, 1, -1), intArrayOf(0, -1, -1), intArrayOf(1, 1, 0), intArrayOf(0, -1, 1), intArrayOf(-1, 1, 0), intArrayOf(0, -1, -1))
	}
}