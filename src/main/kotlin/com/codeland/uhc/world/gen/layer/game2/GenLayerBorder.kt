package com.codeland.uhc.world.gen.layer.game2

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer7

class GenLayerBorder : AreaTransformer7 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		fun anyIsnt(value: Int): Int {
			if (p1 != value) return p1
			if (p2 != value) return p2
			if (p3 != value) return p3
			if (p4 != value) return p4
			return -1
		}

		return when (p5) {
			Region.OCEAN.ordinal -> when (anyIsnt(Region.OCEAN.ordinal)) {
				-1 -> Region.OCEAN.ordinal
				Region.MOUNTAINS.ordinal -> Region.STONE_SHORE.ordinal
				Region.SNOWY.ordinal,
				Region.SNOWY_TAIGA.ordinal -> Region.SNOWY_BEACH.ordinal
				Region.SWAMP.ordinal -> Region.SWAMP.ordinal
				else -> Region.BEACH.ordinal
			}
			Region.JUNGLE.ordinal -> when(anyIsnt(Region.JUNGLE.ordinal)) {
				-1 -> Region.JUNGLE.ordinal
				Region.PLAINS.ordinal,
				Region.SAVANNA.ordinal,
				Region.DESERT.ordinal,
				Region.MOUNTAINS.ordinal,
				Region.BIRCH_FOREST.ordinal -> Region.JUNGLE_EDGE.ordinal
				else -> Region.JUNGLE.ordinal
			}
			Region.GIANT_TAIGA.ordinal -> when(anyIsnt(Region.GIANT_TAIGA.ordinal)) {
				-1 -> Region.GIANT_TAIGA.ordinal
				else -> Region.TAIGA.ordinal
			}
			else -> p5
		}
	}
}
