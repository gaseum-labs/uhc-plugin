package com.codeland.uhc.world.gen.layer.game2;

import com.codeland.uhc.world.gen.BiomeNo

enum class Region(val main: Int, val internal: Int, val special: Int) {
	PLAINS(BiomeNo.PLAINS, BiomeNo.FOREST, BiomeNo.SUNFLOWER_PLAINS),
	SWAMP(BiomeNo.SWAMP, BiomeNo.SWAMP, BiomeNo.SWAMP_HILLS),
	DARK_FOREST(BiomeNo.DARK_FOREST, BiomeNo.PLAINS, BiomeNo.DARK_FOREST_HILLS),
	TAIGA(BiomeNo.TAIGA, BiomeNo.TAIGA_HILLS, BiomeNo.TAIGA_MOUNTAINS),
	JUNGLE(BiomeNo.JUNGLE, BiomeNo.JUNGLE_HILLS, BiomeNo.MODIFIED_JUNGLE),
	FOREST(BiomeNo.FOREST, BiomeNo.WOODED_HILLS, BiomeNo.FLOWER_FOREST),
	BIRCH_FOREST(BiomeNo.BIRCH_FOREST, BiomeNo.BIRCH_FOREST_HILLS, BiomeNo.TALL_BIRCH_FOREST),
	MOUNTAINS(BiomeNo.MOUNTAINS, BiomeNo.WOODED_MOUNTAINS, BiomeNo.GRAVELLY_MOUNTAINS),
	
	SNOWY(BiomeNo.SNOWY_TUNDRA, BiomeNo.SNOWY_MOUNTAINS, BiomeNo.ICE_SPIKES),
	SNOWY_TAIGA(BiomeNo.SNOWY_TAIGA, BiomeNo.SNOWY_TAIGA_HILLS, BiomeNo.SNOWY_TAIGA_MOUNTAINS),
	
	SAVANNA(BiomeNo.SAVANNA, BiomeNo.SAVANNA_PLATEAU, BiomeNo.SHATTERED_SAVANNA),
	DESERT(BiomeNo.DESERT, BiomeNo.DESERT_HILLS, BiomeNo.DESERT_LAKES),

	GIANT_TAIGA(BiomeNo.GIANT_TREE_TAIGA, BiomeNo.GIANT_TREE_TAIGA_HILLS, BiomeNo.GIANT_SPRUCE_TAIGA),

	BADLANDS(BiomeNo.BADLANDS, BiomeNo.WOODED_BADLANDS_PLATEAU, BiomeNo.MODIFIED_WOODED_BADLANDS_PLATEAU),
	BADLANDS_PLATEAU(BiomeNo.BADLANDS_PLATEAU, BiomeNo.BADLANDS_PLATEAU, BiomeNo.ERODED_BADLANDS),

	OCEAN(BiomeNo.OCEAN, BiomeNo.OCEAN, BiomeNo.WARM_OCEAN),
	BEACH(BiomeNo.BEACH, BiomeNo.BEACH, BiomeNo.BEACH),
	SNOWY_BEACH(BiomeNo.SNOWY_BEACH, BiomeNo.SNOWY_BEACH, BiomeNo.SNOWY_BEACH),
	JUNGLE_EDGE(BiomeNo.JUNGLE_EDGE, BiomeNo.JUNGLE_EDGE, BiomeNo.MODIFIED_JUNGLE_EDGE),
	STONE_SHORE(BiomeNo.STONE_SHORE, BiomeNo.STONE_SHORE, BiomeNo.STONE_SHORE);

	companion object {
		fun pack(region: Region, special: Boolean): Int {
			return region.ordinal.or((if (special) 1 else 0).shl(31))
		}

		fun unpackRegion(code: Int): Region {
			return values()[code.and(0x7fffffff)]
		}

		fun unpackSpecial(code: Int): Boolean {
			return code.ushr(31) == 1
		}

		fun unpack(code: Int): Pair<Region, Boolean> {
			return Pair(unpackRegion(code), unpackSpecial(code))
		}
	}
}
