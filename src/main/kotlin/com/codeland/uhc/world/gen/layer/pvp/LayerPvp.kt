package com.codeland.uhc.world.gen.layer.pvp

import com.codeland.uhc.world.gen.BiomeNo.BADLANDS
import com.codeland.uhc.world.gen.BiomeNo.BEACH
import com.codeland.uhc.world.gen.BiomeNo.BIRCH_FOREST
import com.codeland.uhc.world.gen.BiomeNo.COLD_OCEAN
import com.codeland.uhc.world.gen.BiomeNo.DARK_FOREST
import com.codeland.uhc.world.gen.BiomeNo.DESERT
import com.codeland.uhc.world.gen.BiomeNo.FLOWER_FOREST
import com.codeland.uhc.world.gen.BiomeNo.FOREST
import com.codeland.uhc.world.gen.BiomeNo.FROZEN_OCEAN
import com.codeland.uhc.world.gen.BiomeNo.FROZEN_RIVER
import com.codeland.uhc.world.gen.BiomeNo.GRAVELLY_MOUNTAINS
import com.codeland.uhc.world.gen.BiomeNo.ICE_SPIKES
import com.codeland.uhc.world.gen.BiomeNo.JUNGLE_EDGE
import com.codeland.uhc.world.gen.BiomeNo.LUKEWARM_OCEAN
import com.codeland.uhc.world.gen.BiomeNo.MOUNTAINS
import com.codeland.uhc.world.gen.BiomeNo.OCEAN
import com.codeland.uhc.world.gen.BiomeNo.PLAINS
import com.codeland.uhc.world.gen.BiomeNo.RIVER
import com.codeland.uhc.world.gen.BiomeNo.SAVANNA
import com.codeland.uhc.world.gen.BiomeNo.SHATTERED_SAVANNA
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_BEACH
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_MOUNTAINS
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_TAIGA
import com.codeland.uhc.world.gen.BiomeNo.SNOWY_TUNDRA
import com.codeland.uhc.world.gen.BiomeNo.STONE_SHORE
import com.codeland.uhc.world.gen.BiomeNo.SUNFLOWER_PLAINS
import com.codeland.uhc.world.gen.BiomeNo.SWAMP
import com.codeland.uhc.world.gen.BiomeNo.TAIGA
import com.codeland.uhc.world.gen.BiomeNo.TALL_BIRCH_FOREST
import com.codeland.uhc.world.gen.BiomeNo.WARM_OCEAN
import com.codeland.uhc.world.gen.BiomeNo.WOODED_BADLANDS_PLATEAU
import com.codeland.uhc.world.gen.BiomeNo.WOODED_MOUNTAINS
import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1

class LayerPvp : AreaTransformer1 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int): Int {
		return when (context.a(13)) {
			0 -> when (context.a(7)) {
				0 -> OCEAN
				1 -> FROZEN_OCEAN
				2 -> WARM_OCEAN
				3 -> LUKEWARM_OCEAN
				4 -> COLD_OCEAN
				5 -> RIVER
				else -> FROZEN_RIVER
			}
			1 -> when (context.a(2)) {
				0 -> PLAINS
				else -> SUNFLOWER_PLAINS
			}
			2 -> when (context.a(2)) {
				0 -> DESERT
				else -> BEACH
			}
			3 -> when (context.a(4)) {
				0 -> MOUNTAINS
				1 -> WOODED_MOUNTAINS
				2 -> GRAVELLY_MOUNTAINS
				else -> STONE_SHORE
			}
			4 -> when (context.a(2)) {
				0 -> FOREST
				else -> FLOWER_FOREST
			}
			5 -> TAIGA
			6 -> when (context.a(2)) {
				0 -> BIRCH_FOREST
				else -> TALL_BIRCH_FOREST
			}
			7 -> when (context.a(2)) {
				0 -> JUNGLE_EDGE
				else -> SWAMP
			}
			8 -> when (context.a(5)) {
				0 -> SNOWY_TUNDRA
				1 -> SNOWY_TAIGA
				2 -> SNOWY_MOUNTAINS
				3 -> SNOWY_BEACH
				else -> ICE_SPIKES
			}
			9 -> DARK_FOREST
			10 -> when (context.a(2)) {
				0 -> SAVANNA
				else -> SHATTERED_SAVANNA
			}
			else -> when (context.a(2)) {
				0 -> BADLANDS
				else -> WOODED_BADLANDS_PLATEAU
			}
		}
	}
}
