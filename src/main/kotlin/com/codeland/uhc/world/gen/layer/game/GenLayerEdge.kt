package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.UHCArea.UHCLayer

class GenLayerEdge(seed: Long, val produce: Int) : UHCLayer(seed) {
	override fun sample(x: Int, z: Int): Int {
		val (p1, p2, p3, p4, pc) = around(x, z)

		return if (p1 != pc || p2 != pc || p3 != pc || p4 != pc) {
			produce
		} else {
			0
		}
	}
}


