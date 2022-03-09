package com.codeland.uhc.world.gen.layer.game;

import com.codeland.uhc.world.gen.BiomeNo
import kotlin.random.Random

enum class Region(vararg biomes: Pair<Int, Int>) {
	FLAT(
		3 to BiomeNo.PLAINS,
		2 to BiomeNo.SWAMP,
		1 to BiomeNo.DESERT,
		1 to BiomeNo.MEADOW,
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
		1 to BiomeNo.SPARSE_JUNGLE,
		1 to BiomeNo.BAMBOO_JUNGLE,
	),

	SPRUCEY(
		4 to BiomeNo.TAIGA,
		1 to BiomeNo.OLD_GROWTH_PINE_TAIGA,
		1 to BiomeNo.OLD_GROWTH_SPRUCE_TAIGA,
	),
	MOUNTAINOUS(
		2 to BiomeNo.WINDSWEPT_FOREST,
		2 to BiomeNo.WINDSWEPT_HILLS,
		1 to BiomeNo.WINDSWEPT_GRAVELLY_HILLS,
	),
	ARID(
		3 to BiomeNo.BADLANDS,
		2 to BiomeNo.WOODED_BADLANDS,
		1 to BiomeNo.ERODED_BADLANDS,
	),
	ACACIA(
		2 to BiomeNo.SAVANNA,
		1 to BiomeNo.WINDSWEPT_SAVANNA,
	),
	SNOWING(
		1 to BiomeNo.SNOWY_SLOPES,
		1 to BiomeNo.SNOWY_TAIGA,
		1 to BiomeNo.SNOWY_PLAINS,
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
