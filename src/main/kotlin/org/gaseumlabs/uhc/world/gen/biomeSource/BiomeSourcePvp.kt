package org.gaseumlabs.uhc.world.gen.biomeSource

import org.gaseumlabs.uhc.world.gen.BiomeNo
import org.gaseumlabs.uhc.world.gen.UHCArea.UHCArea
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.game.*
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.pvp.LayerPvp
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.*

class BiomeSourcePvp(
	val seed: Long,
) : CheckerboardColumnBiomeSource(BiomeNo.biomeHolderSet, 1) {
	private val area = createAreaPvp(seed)

	override fun getNoiseBiome(x: Int, y: Int, z: Int, niose: Climate.Sampler): Holder<Biome> {
		return BiomeNo.fromId(area.sample(x, z))
	}

	private fun createAreaPvp(seed: Long): UHCArea {
		return UHCArea(LayerPvp(seed))
			.addLayer(GenLayerShiftZZoom(seed, 2))
			.addLayer(GenLayerShiftX(seed, 2))
			.addLayer(GenLayerCombiner(seed))
			.addLayer(GenLayerShiftZZoom(seed, 2))
			.addLayer(GenLayerShiftX(seed, 2))
			.addLayer(GenLayerCombiner(seed))
	}
}
