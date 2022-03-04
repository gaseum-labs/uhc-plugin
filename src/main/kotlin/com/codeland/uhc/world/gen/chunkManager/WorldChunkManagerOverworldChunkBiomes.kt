package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.world.level.biome.*
import kotlin.random.Random

class BiomeSourceChunkBiomes(
	val seed: Long,
) : CheckerboardColumnBiomeSource(emptyList(), 1) {
	override fun getNoiseBiome(x: Int, y: Int, z: Int, niose: Climate.Sampler): Biome {
		val chunkX = Util.floorDiv(x, 4)
		val chunkZ = Util.floorDiv(z, 4)

		val random = Random(chunkX.toLong().shl(32).or(chunkZ.toLong().and(0x0000FFFF)).xor(seed))

		return BiomeNo.fromId(biomeList[random.nextInt(biomeList.size)])
	}

	companion object {
		val biomeList = arrayOf(
			BiomeNo.PLAINS,
			BiomeNo.SUNFLOWER_PLAINS,
			BiomeNo.SNOWY_PLAINS,
			BiomeNo.ICE_SPIKES,
			BiomeNo.DESERT,
			BiomeNo.SWAMP,
			BiomeNo.FOREST,
			BiomeNo.FLOWER_FOREST,
			BiomeNo.BIRCH_FOREST,
			BiomeNo.DARK_FOREST,
			BiomeNo.OLD_GROWTH_BIRCH_FOREST,
			BiomeNo.OLD_GROWTH_PINE_TAIGA,
			BiomeNo.OLD_GROWTH_SPRUCE_TAIGA,
			BiomeNo.TAIGA,
			BiomeNo.SNOWY_TAIGA,
			BiomeNo.SAVANNA,
			BiomeNo.SAVANNA_PLATEAU,
			BiomeNo.WINDSWEPT_HILLS,
			BiomeNo.WINDSWEPT_GRAVELLY_HILLS,
			BiomeNo.WINDSWEPT_FOREST,
			BiomeNo.WINDSWEPT_SAVANNA,
			BiomeNo.JUNGLE,
			BiomeNo.SPARSE_JUNGLE,
			BiomeNo.BAMBOO_JUNGLE,
			BiomeNo.BADLANDS,
			BiomeNo.ERODED_BADLANDS,
			BiomeNo.WOODED_BADLANDS,
			BiomeNo.MEADOW,
			BiomeNo.GROVE,
			BiomeNo.SNOWY_SLOPES,
			BiomeNo.FROZEN_PEAKS,
			BiomeNo.JAGGED_PEAKS,
			BiomeNo.STONY_PEAKS,
			BiomeNo.RIVER,
			BiomeNo.FROZEN_RIVER,
			BiomeNo.BEACH,
			BiomeNo.SNOWY_BEACH,
			BiomeNo.STONY_SHORE,
			BiomeNo.WARM_OCEAN,
			BiomeNo.LUKEWARM_OCEAN,
			BiomeNo.DEEP_LUKEWARM_OCEAN,
			BiomeNo.OCEAN,
			BiomeNo.DEEP_OCEAN,
			BiomeNo.COLD_OCEAN,
			BiomeNo.DEEP_COLD_OCEAN,
			BiomeNo.FROZEN_OCEAN,
			BiomeNo.DEEP_FROZEN_OCEAN,
			BiomeNo.MUSHROOM_FIELDS,
			BiomeNo.DRIPSTONE_CAVES,
			BiomeNo.LUSH_CAVES,
			BiomeNo.NETHER_WASTES,
			BiomeNo.WARPED_FOREST,
			BiomeNo.CRIMSON_FOREST,
			BiomeNo.SOUL_SAND_VALLEY,
			BiomeNo.BASALT_DELTAS,
			BiomeNo.THE_END,
			BiomeNo.END_HIGHLANDS,
			BiomeNo.END_MIDLANDS,
			BiomeNo.SMALL_END_ISLANDS,
			BiomeNo.END_BARRENS,
		)
	}
}
