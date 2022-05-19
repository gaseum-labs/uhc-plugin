package org.gaseumlabs.uhc.world.gen.UHCArea.layer.game

import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.gen.UHCArea.UHCLayer
import kotlin.random.Random

class LayerNoise(seed: Long) : UHCLayer(seed) {
	override fun sample(x: Int, z: Int): Int {
		return Random(Util.coordPack(x, z, seed)).nextInt(2)
	}
}


