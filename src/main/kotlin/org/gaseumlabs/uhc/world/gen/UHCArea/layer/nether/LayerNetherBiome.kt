package org.gaseumlabs.uhc.world.gen.UHCArea.layer.nether

import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.gen.BiomeNo
import org.gaseumlabs.uhc.world.gen.UHCArea.UHCLayer

class LayerNetherBiome(seed: Long) : UHCLayer(seed) {
	companion object {
		val specialBiomes = arrayOf(
			BiomeNo.BASALT_DELTAS,
			BiomeNo.SOUL_SAND_VALLEY,
			BiomeNo.CRIMSON_FOREST,
			BiomeNo.WARPED_FOREST
		)
	}

	override fun sample(x: Int, z: Int): Int {
		return if (Util.mod(2 * z + x, 4) == 0) {
			specialBiomes[random(x, z).nextInt(specialBiomes.size)]
		} else {
			BiomeNo.NETHER_WASTES
		}
	}
}
