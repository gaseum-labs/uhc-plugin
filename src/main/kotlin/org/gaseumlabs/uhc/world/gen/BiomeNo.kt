package org.gaseumlabs.uhc.world.gen

import com.mojang.serialization.Lifecycle
import com.sun.jna.platform.unix.Resource
import org.gaseumlabs.uhc.util.reflect.UHCReflect
import net.minecraft.core.*
import net.minecraft.data.BuiltinRegistries
import net.minecraft.data.worldgen.NoiseData
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.synth.NormalNoise
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters
import net.minecraft.world.level.levelgen.synth.NormalNoise.create
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.gaseumlabs.uhc.lobbyPvp.armorEnchants

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

	/* util */

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
		return false
	}

	/* fields */

	val dedicatedServerField = UHCReflect<CraftServer, DedicatedServer>(CraftServer::class, "console")

	lateinit var biomeRegistry: WritableRegistry<Biome>
	lateinit var noiseRegistry: WritableRegistry<NormalNoise.NoiseParameters>
	lateinit var featureBiomes: Map<Int, Holder<Biome>>

	val biomeHolders = HashMap<Int, Holder<Biome>>()
	val nameMap = HashMap<String, Int>()
	lateinit var biomeHolderSet: HolderSet<Biome>

	var initialized = false

	fun delayedInit() {
		if (initialized) return
		initialized = true

		val registryAccess = (Bukkit.getServer() as CraftServer).handle.server.registryAccess()

		biomeRegistry = registryAccess.ownedRegistryOrThrow(Registry.BIOME_REGISTRY) as WritableRegistry<Biome>
		noiseRegistry = registryAccess.ownedRegistryOrThrow(Registry.NOISE_REGISTRY) as WritableRegistry<NoiseParameters>

		resourceKeys.forEachIndexed { i, resourceKey ->
			biomeHolders[i] = biomeRegistry.getOrCreateHolder(resourceKey)
		}

		biomeHolderSet = HolderSet.direct(biomeHolders.entries.map { it.value })

		biomeRegistry.entrySet().toList().forEach { entry ->
			nameMap[entry.key.location().path] = toId(entry.key)
		}

		featureBiomes = ModifiedBiomes.genBiomes(replaceFeatures = true, replaceMobs = true)

		newNoises()
	}

	fun noiseKey(name: String): ResourceKey<NormalNoise.NoiseParameters> {
		return ResourceKey.create(Registry.NOISE_REGISTRY, ResourceLocation(name))
	}

	fun noiseKeyArray(baseName: String, length: Int): Array<ResourceKey<NormalNoise.NoiseParameters>> {
		return Array(length) { i ->
			noiseKey(baseName + "_${i}")
		}
	}

	fun registerNoiseClone(key: ResourceKey<NoiseParameters>, original: NoiseParameters) {
		val doubleArray = DoubleArray(original.amplitudes.size - 1) { i ->
			original.amplitudes.getDouble(i + 1)
		}

		BuiltinRegistries.register(
			BuiltinRegistries.NOISE,
			key,
			NoiseParameters(
				original.firstOctave,
				original.amplitudes.first(),
				*doubleArray
			)
		)
	}

	fun createNoiseCloneArray(keys: Array<ResourceKey<NoiseParameters>>, originalKey: ResourceKey<NoiseParameters>) {
		val original = noiseRegistry.getOrCreateHolder(originalKey).value()

		for (key in keys) {
			registerNoiseClone(key, original)
		}
	}
	val UHC_SAPGHETTI_MODULATOR_KEYS = noiseKeyArray("uhc_spaghetti_modulator", 6)
	val UHC_SAPGHETTI_KEYS = noiseKeyArray("uhc_spaghetti", 6)
	val UHC_SAPGHETTI_ELEVATION_KEYS = noiseKeyArray("uhc_spaghetti_elevation", 6)
	val UHC_SAPGHETTI_THICKNESS_KEYS = noiseKeyArray("uhc_spaghetti_thickness", 6)

	fun newNoises() {
		createNoiseCloneArray(UHC_SAPGHETTI_MODULATOR_KEYS, Noises.SPAGHETTI_2D_MODULATOR)
		createNoiseCloneArray(UHC_SAPGHETTI_KEYS, Noises.SPAGHETTI_2D)
		createNoiseCloneArray(UHC_SAPGHETTI_ELEVATION_KEYS, Noises.SPAGHETTI_2D_ELEVATION)
		createNoiseCloneArray(UHC_SAPGHETTI_THICKNESS_KEYS, Noises.SPAGHETTI_2D_THICKNESS)
	}
}
