package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.server.v1_16_R3.*

class GenLayerHillApply : AreaTransformer3, AreaTransformerIdentity {
	override fun a(p0: WorldGenContext, biomeArea: Area, hillArea: Area, x: Int, z: Int): Int {
		val baseBiome = biomeArea.a(a(x), b(z))

		return if (hillArea.a(a(x), b(z)) == 1) {
			when (baseBiome) {
				BiomeNo.SNOWY_TUNDRA -> BiomeNo.SNOWY_MOUNTAINS
				BiomeNo.ICE_SPIKES -> BiomeNo.ICE_SPIKES
				BiomeNo.SNOWY_TAIGA -> BiomeNo.SNOWY_MOUNTAINS
				BiomeNo.SNOWY_TAIGA_MOUNTAINS -> BiomeNo.SNOWY_MOUNTAINS
				BiomeNo.TAIGA -> BiomeNo.MOUNTAINS
				BiomeNo.GIANT_TREE_TAIGA -> BiomeNo.GIANT_TREE_TAIGA_HILLS
				BiomeNo.SWAMP -> BiomeNo.MOUNTAINS
				BiomeNo.SWAMP_HILLS -> BiomeNo.STONE_SHORE
				BiomeNo.DARK_FOREST -> BiomeNo.GRAVELLY_MOUNTAINS
				BiomeNo.DARK_FOREST_HILLS -> BiomeNo.MODIFIED_GRAVELLY_MOUNTAINS
				BiomeNo.BIRCH_FOREST -> BiomeNo.GRAVELLY_MOUNTAINS
				BiomeNo.TALL_BIRCH_FOREST -> BiomeNo.TALL_BIRCH_HILLS
				BiomeNo.FOREST -> BiomeNo.WOODED_MOUNTAINS
				BiomeNo.FLOWER_FOREST -> BiomeNo.MODIFIED_GRAVELLY_MOUNTAINS
				BiomeNo.PLAINS -> BiomeNo.MOUNTAINS
				BiomeNo.SUNFLOWER_PLAINS -> BiomeNo.WOODED_MOUNTAINS
				BiomeNo.BADLANDS -> BiomeNo.WOODED_BADLANDS_PLATEAU
				BiomeNo.ERODED_BADLANDS -> BiomeNo.MODIFIED_WOODED_BADLANDS_PLATEAU
				BiomeNo.DESERT -> BiomeNo.DESERT_HILLS
				BiomeNo.DESERT_LAKES -> BiomeNo.DESERT_HILLS
				BiomeNo.SAVANNA -> BiomeNo.SAVANNA_PLATEAU
				BiomeNo.SHATTERED_SAVANNA -> BiomeNo.SHATTERED_SAVANNA_PLATEAU
				else -> BiomeNo.MOUNTAINS
			}
		} else {
			baseBiome
		}
	}
}
