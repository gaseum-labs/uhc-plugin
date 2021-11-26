package com.codeland.uhc.world.gen.layer.nether

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1

class LayerNetherBiome(val seed: Long) : AreaTransformer1 {
	companion object {
		val specialBiomes = arrayOf(
			BiomeNo.BASALT_DELTAS,
			BiomeNo.SOUL_SAND_VALLEY,
			BiomeNo.CRIMSON_FOREST,
			BiomeNo.WARPED_FOREST
		)
	}

	override fun a(context: WorldGenContext, x: Int, z: Int): Int {
		return if (Util.mod(2 * z + x, 4) == 0) {
			specialBiomes[context.a(specialBiomes.size)]
		} else {
			BiomeNo.NETHER_WASTES
		}
	}
}
