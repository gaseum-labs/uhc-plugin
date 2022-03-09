package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.BiomeNo
import com.codeland.uhc.world.gen.UHCArea.UHCArea
import com.codeland.uhc.world.gen.layer.game.*
import com.codeland.uhc.world.gen.layer.pvp.LayerPvp
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.world.level.biome.*

class BiomeSourcePvp(
	val seed: Long,
) : CheckerboardColumnBiomeSource(HolderSet.direct(), 1) {
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
