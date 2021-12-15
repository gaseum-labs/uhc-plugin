package com.codeland.uhc.world.gen.layer.game;

import com.codeland.uhc.world.gen.BiomeNo
import kotlin.random.Random

enum class Region(vararg val biomes: Pair<Int, Int>) {
	FLAT(
		3 to BiomeNo.PLAINS,
		2 to BiomeNo.SWAMP,
		1 to BiomeNo.DESERT,
	),
	FORESTED(
		2 to BiomeNo.FOREST,
		1 to BiomeNo.BIRCH_FOREST,
		1 to BiomeNo.DARK_FOREST,
	),

	AQUATIC(
		2 to BiomeNo.OCEAN,
		1 to BiomeNo.LUKEWARM_OCEAN,
		1 to BiomeNo.WARM_OCEAN
	),
	JUNGLEY(
		3 to BiomeNo.JUNGLE,
		1 to BiomeNo.JUNGLE_EDGE,
		1 to BiomeNo.BAMBOO_JUNGLE,
	),

	SPRUCEY(
		4 to BiomeNo.TAIGA,
		1 to BiomeNo.GIANT_TREE_TAIGA,
		1 to BiomeNo.GIANT_SPRUCE_TAIGA,
	),
	MOUNTAINOUS(
		2 to BiomeNo.MOUNTAINS,
		2 to BiomeNo.WOODED_MOUNTAINS,
		1 to BiomeNo.GRAVELLY_MOUNTAINS,
	),
	ARID(
		3 to BiomeNo.BADLANDS,
		2 to BiomeNo.WOODED_BADLANDS_PLATEAU,
		1 to BiomeNo.BADLANDS_PLATEAU,
		1 to BiomeNo.ERODED_BADLANDS,
	),
	ACACIA(
		2 to BiomeNo.SAVANNA,
		1 to BiomeNo.SHATTERED_SAVANNA,
	),
	SNOWING(
		1 to BiomeNo.SNOWY_TUNDRA,
		1 to BiomeNo.SNOWY_TAIGA
	)
	;

	private val list = biomes.flatMap { (count, biome) ->
		Array(count) { biome }.asIterable()
	}

	fun getBiome(random: Random): Int {
		return list[random.nextInt(list.size)]
	}

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
