package org.gaseumlabs.uhc.world.gen.biomeSource

import org.gaseumlabs.uhc.world.gen.BiomeNo
import org.gaseumlabs.uhc.world.gen.UHCArea.UHCArea
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.game.*
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.nether.LayerNetherBiome
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.world.level.biome.*

class BiomeSourceNether(
	val seed: Long,
	val features: Boolean,
	val area: UHCArea,
) : CheckerboardColumnBiomeSource(HolderSet.direct(), 1) {
	override fun getNoiseBiome(x: Int, y: Int, z: Int, niose: Climate.Sampler): Holder<Biome> {
		val biomeId = area.sample(x, z)

		return if (features) BiomeNo.featureBiomes[biomeId]!!
		else BiomeNo.fromId(biomeId)
	}

	companion object {
		fun createFeaturesPair(
			seed: Long,
		): Pair<BiomeSourceNether, BiomeSourceNether> {
			val area = createAreaNether(seed)

			return Pair(
				BiomeSourceNether(seed, false, area),
				BiomeSourceNether(seed, true, area)
			)
		}

		private fun createAreaNether(seed: Long): UHCArea {
			return UHCArea(LayerNetherBiome(seed))  /* 4X */
				.addLayer(GenLayerShiftZZoom(seed, 3))
				.addLayer(GenLayerShiftX(seed, 3))
				.addLayer(GenLayerCombiner(seed))
				.addLayer(GenLayerShiftZZoom(seed, 3))
				.addLayer(GenLayerShiftX(seed, 3))
				.addLayer(GenLayerCombiner(seed))
		}
	}
}
