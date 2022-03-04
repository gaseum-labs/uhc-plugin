package com.codeland.uhc.world.gen.layer.pvp

import com.codeland.uhc.world.gen.BiomeNo.BADLANDS
import com.codeland.uhc.world.gen.BiomeNo.BIRCH_FOREST
import com.codeland.uhc.world.gen.BiomeNo.DARK_FOREST
import com.codeland.uhc.world.gen.BiomeNo.DESERT
import com.codeland.uhc.world.gen.BiomeNo.FLOWER_FOREST
import com.codeland.uhc.world.gen.BiomeNo.FOREST
import com.codeland.uhc.world.gen.BiomeNo.GRAVELLY_MOUNTAINS
import com.codeland.uhc.world.gen.BiomeNo.ICE_SPIKES
import com.codeland.uhc.world.gen.BiomeNo.JUNGLE_EDGE
import com.codeland.uhc.world.gen.BiomeNo.MOUNTAINS
import com.codeland.uhc.world.gen.BiomeNo.PLAINS
import com.codeland.uhc.world.gen.BiomeNo.SAVANNA
import com.codeland.uhc.world.gen.BiomeNo.SHATTERED_SAVANNA
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_BEACH
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_TAIGA
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_TUNDRA
import com.codeland.uhc.world.gen.BiomeNo.STONE_SHORE
import com.codeland.uhc.world.gen.BiomeNo.SUNFLOWER_PLAINS
import com.codeland.uhc.world.gen.BiomeNo.SWAMP
import com.codeland.uhc.world.gen.BiomeNo.TAIGA
import com.codeland.uhc.world.gen.BiomeNo.TALL_BIRCH_FOREST
import com.codeland.uhc.world.gen.BiomeNo.WOODED_BADLANDS_PLATEAU
import com.codeland.uhc.world.gen.BiomeNo.WOODED_MOUNTAINS
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
				0 -> MOUNTAINS
				1 -> WOODED_MOUNTAINS
				2 -> GRAVELLY_MOUNTAINS
				else -> STONE_SHORE
			}
			3 -> when (random.nextInt(2)) {
				0 -> FOREST
				else -> FLOWER_FOREST
			}
			4 -> TAIGA
			5 -> when (random.nextInt(2)) {
				0 -> BIRCH_FOREST
				else -> TALL_BIRCH_FOREST
			}
			6 -> when (random.nextInt(2)) {
				0 -> JUNGLE_EDGE
				else -> SWAMP
			}
			7 -> when (random.nextInt(4)) {
				0 -> SNOWY_TUNDRA
				1 -> SNOWY_TAIGA
				2 -> SNOWY_BEACH
				else -> ICE_SPIKES
			}
			8 -> DARK_FOREST
			9 -> when (random.nextInt(2)) {
				0 -> SAVANNA
				else -> SHATTERED_SAVANNA
			}
			else -> when (random.nextInt(2)) {
				0 -> BADLANDS
				else -> WOODED_BADLANDS_PLATEAU
			}
		}
	}
}
