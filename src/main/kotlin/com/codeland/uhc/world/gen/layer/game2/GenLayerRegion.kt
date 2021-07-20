package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerRegion : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return when (p5) {
			0 -> coldRegions[context.a(coldRegions.size)]
			1 -> hotRegions[context.a(hotRegions.size)]
			2 -> temperateRegions[context.a(temperateRegions.size)]
			else -> Region.GIANT_TAIGA.ordinal
		}
	}

	val temperateRegions = arrayOf(
		Region.PLAINS.ordinal,
		Region.SWAMP.ordinal,
		Region.DARK_FOREST.ordinal,
		Region.TAIGA.ordinal,
		Region.JUNGLE.ordinal,
		Region.FOREST.ordinal,
		Region.BIRCH_FOREST.ordinal,
		Region.MOUNTAINS.ordinal
	)

	val hotRegions = arrayOf(
		Region.SAVANNA.ordinal,
		Region.DESERT.ordinal,
		Region.BADLANDS.ordinal
	)

	val coldRegions = arrayOf(
		Region.SNOWY.ordinal,
		Region.SNOWY_TAIGA.ordinal
	)
}


