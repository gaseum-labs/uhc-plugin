package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.UHCArea.UHCLayer
import kotlin.random.Random

class GenLayerOffset(seed: Long, val scale: Int) : UHCLayer(seed) {
	override fun sample(x: Int, z: Int): Int {
		val random = Random(seed)
		val offX = random.nextInt(-scale, scale)
		val offZ = random.nextInt(-scale, scale)

		return previous.sample(x + offX, z + offZ)
	}
}
