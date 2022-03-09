package com.codeland.uhc.world.gen.layer.pvp

import com.codeland.uhc.world.gen.BiomeNo.BADLANDS
import com.codeland.uhc.world.gen.BiomeNo.BIRCH_FOREST
import com.codeland.uhc.world.gen.BiomeNo.DARK_FOREST
import com.codeland.uhc.world.gen.BiomeNo.DESERT
import com.codeland.uhc.world.gen.BiomeNo.FLOWER_FOREST
import com.codeland.uhc.world.gen.BiomeNo.FOREST
import com.codeland.uhc.world.gen.BiomeNo.ICE_SPIKES
import com.codeland.uhc.world.gen.BiomeNo.OLD_GROWTH_BIRCH_FOREST
import com.codeland.uhc.world.gen.BiomeNo.PLAINS
import com.codeland.uhc.world.gen.BiomeNo.SAVANNA
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_BEACH
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_PLAINS
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_TAIGA
import com.codeland.uhc.world.gen.BiomeNo.SPARSE_JUNGLE
import com.codeland.uhc.world.gen.BiomeNo.STONY_SHORE
import com.codeland.uhc.world.gen.BiomeNo.SUNFLOWER_PLAINS
import com.codeland.uhc.world.gen.BiomeNo.SWAMP
import com.codeland.uhc.world.gen.BiomeNo.TAIGA
import com.codeland.uhc.world.gen.BiomeNo.WINDSWEPT_FOREST
import com.codeland.uhc.world.gen.BiomeNo.WINDSWEPT_GRAVELLY_HILLS
import com.codeland.uhc.world.gen.BiomeNo.WINDSWEPT_HILLS
import com.codeland.uhc.world.gen.BiomeNo.WINDSWEPT_SAVANNA
import com.codeland.uhc.world.gen.BiomeNo.WOODED_BADLANDS
import com.codeland.uhc.world.gen.UHCArea.UHCLayer

class LayerPvp(seed: Long) : UHCLayer(seed) {
	override fun sample(x: Int, z: Int): Int {
		val random = random(x, z)

		return when (random.nextInt(11)) {
			0 -> when (random.nextInt(2)) {
				0 -> PLAINS
				else -> SUNFLOWER_PLAINS
			}
			1 -> DESERT
			2 -> when (random.nextInt(4)) {
				0 -> WINDSWEPT_HILLS
				1 -> WINDSWEPT_FOREST
				2 -> WINDSWEPT_GRAVELLY_HILLS
				else -> STONY_SHORE
			}
			3 -> when (random.nextInt(2)) {
				0 -> FOREST
				else -> FLOWER_FOREST
			}
			4 -> TAIGA
			5 -> when (random.nextInt(2)) {
				0 -> BIRCH_FOREST
				else -> OLD_GROWTH_BIRCH_FOREST
			}
			6 -> when (random.nextInt(2)) {
				0 -> SPARSE_JUNGLE
				else -> SWAMP
			}
			7 -> when (random.nextInt(4)) {
				0 -> SNOWY_PLAINS
				1 -> SNOWY_TAIGA
				2 -> SNOWY_BEACH
				else -> ICE_SPIKES
			}
			8 -> DARK_FOREST
			9 -> when (random.nextInt(2)) {
				0 -> SAVANNA
				else -> WINDSWEPT_SAVANNA
			}
			else -> when (random.nextInt(2)) {
				0 -> BADLANDS
				else -> WOODED_BADLANDS
			}
		}
	}
}
