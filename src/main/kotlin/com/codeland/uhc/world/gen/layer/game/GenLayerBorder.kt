package com.codeland.uhc.world.gen.layer.game

import net.minecraft.world.level.newbiome.context.WorldGenContext
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer7

class GenLayerBorder : AreaTransformer7 {
	override fun a(context: WorldGenContext, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int): Int {
		fun isntRegion(code: Int, region: Region) = if (Region.unpackRegion(code) !== region)
			Region.unpackRegion(code)
		else
			null

		fun anyIsnt(region: Region) = isntRegion(p1, region)
			?: isntRegion(p2, region)
			?: isntRegion(p3, region)
			?: isntRegion(p4, region)

		val (originalRegion, special) = Region.unpack(p5)

		val swapRegion = when (originalRegion) {
			Region.OCEAN -> when (anyIsnt(Region.OCEAN)) {
				null -> null
				Region.MOUNTAINS -> Region.STONE_SHORE
				Region.SNOWY,
				Region.SNOWY_TAIGA,
				-> Region.SNOWY_BEACH
				Region.SWAMP -> Region.SWAMP
				else -> Region.BEACH
			}
			Region.JUNGLE -> when (anyIsnt(Region.JUNGLE)) {
				null -> null
				Region.PLAINS,
				Region.SAVANNA,
				Region.DESERT,
				Region.MOUNTAINS,
				Region.BIRCH_FOREST,
				-> Region.JUNGLE_EDGE
				else -> Region.JUNGLE
			}
			Region.GIANT_TAIGA -> when (anyIsnt(Region.GIANT_TAIGA)) {
				null -> null
				else -> Region.TAIGA
			}
			else -> null
		}

		return Region.pack(swapRegion ?: originalRegion, special)
	}
}
