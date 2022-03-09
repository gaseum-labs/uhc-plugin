package com.codeland.uhc.world.gen

import com.codeland.uhc.util.UHCReflect
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.levelgen.synth.NormalNoise
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R2.CraftServer

object BiomeNo {
	const val THE_VOID = 0
	const val PLAINS = 1
	const val SUNFLOWER_PLAINS = 2
	const val SNOWY_PLAINS = 3
	const val ICE_SPIKES = 4
	const val DESERT = 5
	const val SWAMP = 6
	const val FOREST = 7
	const val FLOWER_FOREST = 8
	const val BIRCH_FOREST = 9
	const val DARK_FOREST = 10
	const val OLD_GROWTH_BIRCH_FOREST = 11
	const val OLD_GROWTH_PINE_TAIGA = 12
	const val OLD_GROWTH_SPRUCE_TAIGA = 13
	const val TAIGA = 14
	const val SNOWY_TAIGA = 15
	const val SAVANNA = 16
	const val SAVANNA_PLATEAU = 17
	const val WINDSWEPT_HILLS = 18
	const val WINDSWEPT_GRAVELLY_HILLS = 19
	const val WINDSWEPT_FOREST = 20
	const val WINDSWEPT_SAVANNA = 21
	const val JUNGLE = 22
	const val SPARSE_JUNGLE = 23
	const val BAMBOO_JUNGLE = 24
	const val BADLANDS = 25
	const val ERODED_BADLANDS = 26
	const val WOODED_BADLANDS = 27
	const val MEADOW = 28
	const val GROVE = 29
	const val SNOWY_SLOPES = 30
	const val FROZEN_PEAKS = 31
	const val JAGGED_PEAKS = 32
	const val STONY_PEAKS = 33
	const val RIVER = 34
	const val FROZEN_RIVER = 35
	const val BEACH = 36
	const val SNOWY_BEACH = 37
	const val STONY_SHORE = 38
	const val WARM_OCEAN = 39
	const val LUKEWARM_OCEAN = 40
	const val DEEP_LUKEWARM_OCEAN = 41
	const val OCEAN = 42
	const val DEEP_OCEAN = 43
	const val COLD_OCEAN = 44
	const val DEEP_COLD_OCEAN = 45
	const val FROZEN_OCEAN = 46
	const val DEEP_FROZEN_OCEAN = 47
	const val MUSHROOM_FIELDS = 48
	const val DRIPSTONE_CAVES = 49
	const val LUSH_CAVES = 50
	const val NETHER_WASTES = 51
	const val WARPED_FOREST = 52
	const val CRIMSON_FOREST = 53
	const val SOUL_SAND_VALLEY = 54
	const val BASALT_DELTAS = 55
	const val THE_END = 56
	const val END_HIGHLANDS = 57
	const val END_MIDLANDS = 58
	const val SMALL_END_ISLANDS = 59
	const val END_BARRENS = 60

	val resourceKeys = arrayOf(
		Biomes.THE_VOID,
		Biomes.PLAINS,
		Biomes.SUNFLOWER_PLAINS,
		Biomes.SNOWY_PLAINS,
		Biomes.ICE_SPIKES,
		Biomes.DESERT,
		Biomes.SWAMP,
		Biomes.FOREST,
		Biomes.FLOWER_FOREST,
		Biomes.BIRCH_FOREST,
		Biomes.DARK_FOREST,
		Biomes.OLD_GROWTH_BIRCH_FOREST,
		Biomes.OLD_GROWTH_PINE_TAIGA,
		Biomes.OLD_GROWTH_SPRUCE_TAIGA,
		Biomes.TAIGA,
		Biomes.SNOWY_TAIGA,
		Biomes.SAVANNA,
		Biomes.SAVANNA_PLATEAU,
		Biomes.WINDSWEPT_HILLS,
		Biomes.WINDSWEPT_GRAVELLY_HILLS,
		Biomes.WINDSWEPT_FOREST,
		Biomes.WINDSWEPT_SAVANNA,
		Biomes.JUNGLE,
		Biomes.SPARSE_JUNGLE,
		Biomes.BAMBOO_JUNGLE,
		Biomes.BADLANDS,
		Biomes.ERODED_BADLANDS,
		Biomes.WOODED_BADLANDS,
		Biomes.MEADOW,
		Biomes.GROVE,
		Biomes.SNOWY_SLOPES,
		Biomes.FROZEN_PEAKS,
		Biomes.JAGGED_PEAKS,
		Biomes.STONY_PEAKS,
		Biomes.RIVER,
		Biomes.FROZEN_RIVER,
		Biomes.BEACH,
		Biomes.SNOWY_BEACH,
		Biomes.STONY_SHORE,
		Biomes.WARM_OCEAN,
		Biomes.LUKEWARM_OCEAN,
		Biomes.DEEP_LUKEWARM_OCEAN,
		Biomes.OCEAN,
		Biomes.DEEP_OCEAN,
		Biomes.COLD_OCEAN,
		Biomes.DEEP_COLD_OCEAN,
		Biomes.FROZEN_OCEAN,
		Biomes.DEEP_FROZEN_OCEAN,
		Biomes.MUSHROOM_FIELDS,
		Biomes.DRIPSTONE_CAVES,
		Biomes.LUSH_CAVES,
		Biomes.NETHER_WASTES,
		Biomes.WARPED_FOREST,
		Biomes.CRIMSON_FOREST,
		Biomes.SOUL_SAND_VALLEY,
		Biomes.BASALT_DELTAS,
		Biomes.THE_END,
		Biomes.END_HIGHLANDS,
		Biomes.END_MIDLANDS,
		Biomes.SMALL_END_ISLANDS,
		Biomes.END_BARRENS,
	)

	val dedicatedServerField = UHCReflect<CraftServer, DedicatedServer>(CraftServer::class, "console")

	val biomeRegistry: Registry<Biome>
	val noiseRegistry: Registry<NormalNoise.NoiseParameters>

	init {
		val registryHolder = dedicatedServerField.get(Bukkit.getServer() as CraftServer).registryHolder

		biomeRegistry = registryHolder.ownedRegistryOrThrow(Registry.BIOME_REGISTRY)
		noiseRegistry = registryHolder.ownedRegistryOrThrow(Registry.NOISE_REGISTRY)
	}

	val biomeHolders = HashMap<Int, Holder<Biome>>()

	init {
		resourceKeys.forEachIndexed { i, resourceKey ->
			biomeHolders[i] = Holder.direct(biomeRegistry.get(resourceKey)!!)
		}
	}

	private val nameMap = HashMap<String, Int>()

	init {
		biomeRegistry.entrySet().toList().forEach { entry ->
			println(entry.key.location().path)
			nameMap[entry.key.location().path] = toId(entry.key)
		}
	}

	fun fromName(name: String?): Int? {
		return nameMap[name]
	}

	fun fromId(id: Int): Holder<Biome> {
		return biomeHolders[id]!!
	}

	fun toId(key: ResourceKey<Biome>): Int {
		return resourceKeys.indexOfFirst { it === key }
	}

	fun isNetherBiome(no: Int): Boolean {
		return when (no) {
			NETHER_WASTES,
			SOUL_SAND_VALLEY,
			CRIMSON_FOREST,
			WARPED_FOREST,
			BASALT_DELTAS,
			-> true
			else -> false
		}
	}

	fun isBadlands(no: Int): Boolean {
		return when (no) {
			BADLANDS,
			ERODED_BADLANDS,
			-> true
			else -> false
		}
	}
}
