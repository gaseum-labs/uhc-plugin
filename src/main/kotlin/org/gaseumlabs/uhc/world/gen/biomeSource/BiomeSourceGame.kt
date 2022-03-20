package org.gaseumlabs.uhc.world.gen.biomeSource

import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource
import net.minecraft.world.level.biome.Climate
import org.gaseumlabs.uhc.world.gen.BiomeNo
import org.gaseumlabs.uhc.world.gen.UHCArea.UHCArea
import org.gaseumlabs.uhc.world.gen.UHCArea.layer.game.*
import kotlin.math.abs

class BiomeSourceGame(
	val seed: Long,
	val centerBiomeNo: Int?,
	val endRadius: Int,
	val biomeset: Map<Int, Holder<Biome>>,
	val area: UHCArea,
) : CheckerboardColumnBiomeSource(
	BiomeNo.createHolderSet(biomeset),
	1
) {
	private val centerBiome = if (centerBiomeNo == null) null else biomeset[centerBiomeNo]!!

	override fun getNoiseBiome(x: Int, y: Int, z: Int, noise: Climate.Sampler): Holder<Biome> {
		/* center biome area */
		return if (centerBiome != null && inRange(x, z, endRadius)) {
			centerBiome

		} else {
			/* regular game area */
			biomeset[area.sample(x, z)]!!
		}
	}

	fun inRange(x: Int, z: Int, range: Int): Boolean {
		return abs(x) <= range / 4 && abs(z) <= range / 4
	}

	companion object {
		fun createAreaGame(seed: Long): UHCArea {
			val area = UHCArea(LayerPerPlayer(seed))
				.addLayer(GenLayerShiftZZoom(seed, 3))
				.addLayer(GenLayerShiftX(seed, 3))
				.addLayer(GenLayerCombiner(seed))
				.addLayer(GenLayerShiftZZoom(seed, 2))
				.addLayer(GenLayerShiftX(seed, 2))
				.addLayer(GenLayerCombiner(seed))

			/* ----------------------------------------------------------------- */

			val riverArea = UHCArea(LayerNoise(seed))
				.addLayer(GenLayerShiftZZoom(seed, 3))
				.addLayer(GenLayerShiftX(seed, 3))
				.addLayer(GenLayerCombiner(seed))
				.addLayer(GenLayerShiftZZoom(seed, 2))
				.addLayer(GenLayerShiftX(seed, 2))
				.addLayer(GenLayerCombiner(seed))
				.addLayer(GenLayerEdge(seed, BiomeNo.RIVER))

			return area.merge(riverArea, GenLayerRiverApply(seed)).addLayer(GenLayerOffset(seed, 36))
		}
	}
}
