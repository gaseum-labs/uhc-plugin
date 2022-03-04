package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.gen.UHCArea.UHCLayer
import kotlin.random.Random

class LayerNoise(seed: Long) : UHCLayer(seed) {
	override fun sample(x: Int, z: Int): Int {
		return Random(Util.coordPack(x, z, seed)).nextInt(2)
	}
}


