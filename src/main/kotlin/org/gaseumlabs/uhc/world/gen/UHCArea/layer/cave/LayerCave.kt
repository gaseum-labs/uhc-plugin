package org.gaseumlabs.uhc.world.gen.UHCArea.layer.cave

import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.gen.BiomeNo
import org.gaseumlabs.uhc.world.gen.UHCArea.UHCLayer

class LayerCave(seed: Long) : UHCLayer(seed) {
	val caveBiomes = arrayOf(
		BiomeNo.LUSH_CAVES,
		BiomeNo.DRIPSTONE_CAVES
	)

	override fun sample(x: Int, z: Int): Int {
		return if (Util.mod(x, 2) == 1 && Util.mod(z, 2) == 1) {
			caveBiomes[random(x, z).nextInt(2)]
		} else {
			BiomeNo.THE_VOID
		}
	}
}