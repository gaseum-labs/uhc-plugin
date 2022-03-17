package org.gaseumlabs.uhc.world.gen.biomeSource

import org.gaseumlabs.uhc.world.gen.BiomeNo
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.world.level.biome.*

class BiomeSourceSingle(
	val seed: Long,
	singleBiomeNo: Int,
) : CheckerboardColumnBiomeSource(HolderSet.direct(), 1) {
	private val singleBiome = BiomeNo.fromId(singleBiomeNo)

	override fun getNoiseBiome(x: Int, y: Int, z: Int, niose: Climate.Sampler): Holder<Biome> {
		return singleBiome
	}
}
