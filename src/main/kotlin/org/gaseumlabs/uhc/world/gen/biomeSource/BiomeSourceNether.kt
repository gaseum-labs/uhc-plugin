package org.gaseumlabs.uhc.world.gen.biomeSource

import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource
import net.minecraft.world.level.biome.Climate
import org.gaseumlabs.uhc.world.gen.UHCArea.UHCArea
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.game.GenLayerCombiner
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.game.GenLayerShiftX
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.game.GenLayerShiftZZoom
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.nether.LayerNetherBiome

class BiomeSourceNether(
	val seed: Long,
	val biomeset: Map<Int, Holder<Biome>>,
	val area: UHCArea,
) : CheckerboardColumnBiomeSource(HolderSet.direct(), 1) {
	override fun getNoiseBiome(x: Int, y: Int, z: Int, noise: Climate.Sampler): Holder<Biome> {
		val biomeId = area.sample(x, z)

		return biomeset[biomeId]!!
	}

	companion object {
		fun createAreaNether(seed: Long): UHCArea {
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
