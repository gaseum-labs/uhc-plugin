package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer4

class GenLayerRegion : AreaTransformer4 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		return Region.pack(when (p5) {
			Temperature.COLD.ordinal -> coldRegions[context.a(coldRegions.size)]
			Temperature.HOT.ordinal -> hotRegions[context.a(hotRegions.size)]
			Temperature.BADLANDS.ordinal -> badlandsRegions[context.a(badlandsRegions.size)]
			Temperature.MEGA.ordinal -> Region.GIANT_TAIGA

			else -> temperateRegions[context.a(temperateRegions.size)]
		}, context.a(4) == 0)
	}

	val temperateRegions = arrayOf(
		Region.PLAINS,
		Region.SWAMP,
		Region.DARK_FOREST,
		Region.TAIGA,
		Region.JUNGLE,
		Region.FOREST,
		Region.BIRCH_FOREST,
		Region.MOUNTAINS
	)

	val hotRegions = arrayOf(
		Region.SAVANNA,
		Region.DESERT,
		Region.PLAINS
	)

	val coldRegions = arrayOf(
		Region.SNOWY,
		Region.SNOWY_TAIGA
	)

	val badlandsRegions = arrayOf(
		Region.BADLANDS,
		Region.BADLANDS_PLATEAU
	)
}
