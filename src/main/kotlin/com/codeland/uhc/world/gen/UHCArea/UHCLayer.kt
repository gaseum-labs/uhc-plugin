package com.codeland.uhc.world.gen.UHCArea

import com.codeland.uhc.util.Util
import kotlin.random.Random

abstract class UHCLayer(val seed: Long) {
	lateinit var previous: UHCLayer

	abstract fun sample(x: Int, z: Int): Int

	data class AroundReturn(val p1: Int, val p2: Int, val p3: Int, val p4: Int, val pc: Int)

	fun around(x: Int, z: Int): AroundReturn {
		return AroundReturn(
			previous.sample(x - 1, z),
			previous.sample(x, z + 1),
			previous.sample(x + 1, z),
			previous.sample(x, z - 1),
			previous.sample(x, z)
		)
	}

	fun random(x: Int, z: Int): Random {
		return Random(Util.coordPack(x, z, seed))
	}
}
