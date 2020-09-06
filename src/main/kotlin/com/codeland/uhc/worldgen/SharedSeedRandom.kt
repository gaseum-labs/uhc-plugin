package com.codeland.uhc.worldgen

import java.util.*

internal class SharedSeedRandom : Random {
	constructor() {}
	constructor(p_i48691_1_: Long) : super(p_i48691_1_) {}

	fun func_202422_a(p_202422_1_: Int, p_202422_2_: Int): Long {
		val i = p_202422_1_ * 341873128712L + p_202422_2_ * 132897987541L
		setSeed(i)
		return i
	}

	fun func_202423_a(p_202423_1_: Int) {
		for (i in 0 until p_202423_1_) {
			next(1)
		}
	}

	fun func_202424_a(p_202424_1_: Long, p_202424_3_: Int, p_202424_4_: Int): Long {
		setSeed(p_202424_1_)
		val i = nextLong() or 1L
		val j = nextLong() or 1L
		val k = p_202424_3_ * i + p_202424_4_ * j xor p_202424_1_
		setSeed(k)
		return k
	}

	fun func_202425_c(p_202425_1_: Long, p_202425_3_: Int, p_202425_4_: Int): Long {
		setSeed(p_202425_1_)
		val i = nextLong()
		val j = nextLong()
		val k = p_202425_3_ * i xor p_202425_4_ * j xor p_202425_1_
		setSeed(k)
		return k
	}

	fun func_202426_b(p_202426_1_: Long, p_202426_3_: Int, p_202426_4_: Int): Long {
		val i = p_202426_1_ + p_202426_3_ + 10000 * p_202426_4_
		setSeed(i)
		return i
	}

	fun func_202427_a(p_202427_1_: Long, p_202427_3_: Int, p_202427_4_: Int, p_202427_5_: Int): Long {
		val i = p_202427_3_ * 341873128712L + p_202427_4_ * 132897987541L + p_202427_1_ + p_202427_5_
		setSeed(i)
		return i
	}

	override fun next(p_next_1_: Int): Int {
		return super.next(p_next_1_)
	}

	companion object {
		private const val serialVersionUID = 1L
		fun func_205190_a(p_205190_0_: Int, p_205190_1_: Int, p_205190_2_: Long, p_205190_4_: Long): Random {
			return Random(p_205190_2_ + p_205190_0_ * p_205190_0_ * 4987142 + p_205190_0_ * 5947611 + p_205190_1_ * p_205190_1_ * 4392871L + p_205190_1_ * 389711 xor p_205190_4_)
		}
	}
}