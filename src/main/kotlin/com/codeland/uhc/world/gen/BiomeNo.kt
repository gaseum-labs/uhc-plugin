package com.codeland.uhc.world.gen

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.resources.MinecraftKey
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.BiomeBase

object BiomeNo {
	const val OCEAN = 0
	const val PLAINS = 1
	const val DESERT = 2
	const val MOUNTAINS = 3
	const val FOREST = 4
	const val TAIGA = 5
	const val SWAMP = 6
	const val RIVER = 7
	const val NETHER_WASTES = 8
	const val THE_END = 9
	const val FROZEN_OCEAN = 10
	const val FROZEN_RIVER = 11
	const val SNOWY_TUNDRA = 12
	const val SNOWY_MOUNTAINS = 13
	const val MUSHROOM_FIELDS = 14
	const val MUSHROOM_FIELD_SHORE = 15
	const val BEACH = 16
	const val DESERT_HILLS = 17
	const val WOODED_HILLS = 18
	const val TAIGA_HILLS = 19
	const val MOUNTAIN_EDGE = 20
	const val JUNGLE = 21
	const val JUNGLE_HILLS = 22
	const val JUNGLE_EDGE = 23
	const val DEEP_OCEAN = 24
	const val STONE_SHORE = 25
	const val SNOWY_BEACH = 26
	const val BIRCH_FOREST = 27
	const val BIRCH_FOREST_HILLS = 28
	const val DARK_FOREST = 29
	const val SNOWY_TAIGA = 30
	const val SNOWY_TAIGA_HILLS = 31
	const val GIANT_TREE_TAIGA = 32
	const val GIANT_TREE_TAIGA_HILLS = 33
	const val WOODED_MOUNTAINS = 34
	const val SAVANNA = 35
	const val SAVANNA_PLATEAU = 36
	const val BADLANDS = 37
	const val WOODED_BADLANDS_PLATEAU = 38
	const val BADLANDS_PLATEAU = 39
	const val SMALL_END_ISLANDS = 40
	const val END_MIDLANDS = 41
	const val END_HIGHLANDS = 42
	const val END_BARRENS = 43
	const val WARM_OCEAN = 44
	const val LUKEWARM_OCEAN = 45
	const val COLD_OCEAN = 46
	const val DEEP_WARM_OCEAN = 47
	const val DEEP_LUKEWARM_OCEAN = 48
	const val DEEP_COLD_OCEAN = 49
	const val DEEP_FROZEN_OCEAN = 50
	const val THE_VOID = 127
	const val SUNFLOWER_PLAINS = 129
	const val DESERT_LAKES = 130
	const val GRAVELLY_MOUNTAINS = 131
	const val FLOWER_FOREST = 132
	const val TAIGA_MOUNTAINS = 133
	const val SWAMP_HILLS = 134
	const val ICE_SPIKES = 140
	const val MODIFIED_JUNGLE = 149
	const val MODIFIED_JUNGLE_EDGE = 151
	const val TALL_BIRCH_FOREST = 155
	const val TALL_BIRCH_HILLS = 156
	const val DARK_FOREST_HILLS = 157
	const val SNOWY_TAIGA_MOUNTAINS = 158
	const val GIANT_SPRUCE_TAIGA = 160
	const val GIANT_SPRUCE_TAIGA_HILLS = 161
	const val MODIFIED_GRAVELLY_MOUNTAINS = 162
	const val SHATTERED_SAVANNA = 163
	const val SHATTERED_SAVANNA_PLATEAU = 164
	const val ERODED_BADLANDS = 165
	const val MODIFIED_WOODED_BADLANDS_PLATEAU = 166
	const val MODIFIED_BADLANDS_PLATEAU = 167
	const val BAMBOO_JUNGLE = 168
	const val BAMBOO_JUNGLE_HILLS = 169
	const val SOUL_SAND_VALLEY = 170
	const val CRIMSON_FOREST = 171
	const val WARPED_FOREST = 172
	const val BASALT_DELTAS = 173

	private val biomeMapField = BiomeRegistry::class.java.getDeclaredField("c")
	private val minecraftKeyField = ResourceKey::class.java.getDeclaredField("c")
	private val nameField = MinecraftKey::class.java.getDeclaredField("f")

	private val nameMap = HashMap<String, Int>()
	private val idMap = HashMap<Int, BiomeBase>()

	init {
		biomeMapField.isAccessible = true
		minecraftKeyField.isAccessible = true
		nameField.isAccessible = true

		val biomeMap = biomeMapField[null] as Int2ObjectMap<ResourceKey<BiomeBase>>

		val biomeRegistry = ModifiedBiomes.biomeRegistryField[null] as IRegistry<BiomeBase>

		biomeMap.forEach { (id, resourceKey) ->
			nameMap[nameField[minecraftKeyField[resourceKey]] as String] = id
			idMap[id] = biomeRegistry.d(resourceKey)
		}
	}

	fun fromName(name: String?): Int? {
		return nameMap[name]
	}

	fun fromId(id: Int): BiomeBase? {
		return idMap[id]
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
}
