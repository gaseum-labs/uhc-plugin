package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.server.v1_16_R3.*

class GenLayerBiomeEdge : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		fun around(biome: Int): Boolean {
			return p1 == biome || p2 == biome || p3 == biome || p4 == biome
		}

		return when (p5) {
			BiomeNo.FOREST -> when {
				around(BiomeNo.MOUNTAINS) || around(BiomeNo.MODIFIED_GRAVELLY_MOUNTAINS) -> BiomeNo.WOODED_HILLS
				else -> BiomeNo.FOREST
			}
			BiomeNo.TAIGA -> when {
				around(BiomeNo.GIANT_SPRUCE_TAIGA_HILLS) -> BiomeNo.TAIGA_HILLS
				around(BiomeNo.MOUNTAINS) -> BiomeNo.TAIGA_MOUNTAINS
				else -> BiomeNo.TAIGA
			}
			BiomeNo.SNOWY_TAIGA -> when {
				around(BiomeNo.MOUNTAINS) || around(BiomeNo.SNOWY_MOUNTAINS) -> BiomeNo.SNOWY_TAIGA_HILLS
				else -> BiomeNo.SNOWY_TAIGA
			}
			BiomeNo.BIRCH_FOREST -> when {
				around(BiomeNo.GRAVELLY_MOUNTAINS) || around(BiomeNo.TALL_BIRCH_HILLS) -> BiomeNo.SNOWY_TAIGA_HILLS
				else -> BiomeNo.BIRCH_FOREST
			}
			BiomeNo.BADLANDS -> when {
				around(BiomeNo.SAVANNA_PLATEAU) || around(BiomeNo.SHATTERED_SAVANNA) -> BiomeNo.BADLANDS_PLATEAU
				else -> BiomeNo.BADLANDS
			}
			BiomeNo.GIANT_TREE_TAIGA -> when {
				around(BiomeNo.DARK_FOREST) || around(BiomeNo.DARK_FOREST_HILLS) -> BiomeNo.GIANT_SPRUCE_TAIGA
				else -> BiomeNo.GIANT_TREE_TAIGA
			}
			else -> p5
		}
	}
}


