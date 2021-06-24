package com.codeland.uhc.world.gen.layer.game

import com.codeland.uhc.world.gen.BiomeNo
import com.codeland.uhc.world.gen.layer.game.GenLayerIdBiome.Companion.id
import com.codeland.uhc.world.gen.layer.game.GenLayerIdBiome.Companion.spec
import com.codeland.uhc.world.gen.layer.game.GenLayerIdBiome.Companion.temp
import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerSpecial : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return if (
			spec(p5) &&
			temp(p1) == temp(p2) &&
			temp(p2) == temp(p3) &&
			temp(p3) == temp(p4) &&
			temp(p4) == temp(p5)
		) {
			when (id(p5)) {
				BiomeNo.SNOWY_TUNDRA -> BiomeNo.ICE_SPIKES
				BiomeNo.SNOWY_TAIGA -> BiomeNo.SNOWY_TAIGA_MOUNTAINS
				BiomeNo.TAIGA -> BiomeNo.GIANT_TREE_TAIGA
				BiomeNo.SWAMP -> BiomeNo.SWAMP_HILLS
				BiomeNo.DARK_FOREST -> BiomeNo.DARK_FOREST_HILLS
				BiomeNo.BIRCH_FOREST -> BiomeNo.TALL_BIRCH_FOREST
				BiomeNo.FOREST -> BiomeNo.FLOWER_FOREST
				BiomeNo.PLAINS -> BiomeNo.SUNFLOWER_PLAINS
				BiomeNo.BADLANDS -> BiomeNo.ERODED_BADLANDS
				BiomeNo.DESERT -> BiomeNo.DESERT_LAKES
				BiomeNo.SAVANNA -> BiomeNo.SHATTERED_SAVANNA
				else -> id(p5)
			}
		} else {
			id(p5)
		}
	}
}
