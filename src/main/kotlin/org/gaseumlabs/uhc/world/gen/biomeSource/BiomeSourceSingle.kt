package org.gaseumlabs.uhc.world.gen.biomeSource

import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource
import net.minecraft.world.level.biome.Climate

class BiomeSourceSingle(
	val seed: Long,
	val biomeset: Map<Int, Holder<Biome>>,
	val singleBiomeNo: Int,
) : CheckerboardColumnBiomeSource(HolderSet.direct(), 1) {
	private val singleBiome = biomeset[singleBiomeNo]!!

	override fun getNoiseBiome(x: Int, y: Int, z: Int, niose: Climate.Sampler): Holder<Biome> {
		return singleBiome
	}
}
