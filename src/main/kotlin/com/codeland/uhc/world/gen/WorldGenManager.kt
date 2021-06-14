package com.codeland.uhc.world.gen

import com.codeland.uhc.core.UHC
import com.codeland.uhc.world.WorldGenOption
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.util.Util
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import java.util.*
import kotlin.collections.HashMap
import com.mojang.datafixers.util.Pair as PairM

object WorldGenManager {
    private val serverWorldsField = CraftServer::class.java.getDeclaredField("worlds")
    private val worldServerField = CraftWorld::class.java.getDeclaredField("world")
    private val chunkProviderServerField = WorldServer::class.java.getDeclaredField("chunkProvider")
    private val chunkGeneratorField = ChunkProviderServer::class.java.getDeclaredField("chunkGenerator")
    private val worldChunkManagerBField = ChunkGenerator::class.java.getDeclaredField("b")
	private val worldChunkManagerCField = ChunkGenerator::class.java.getDeclaredField("c")
    private val hField = WorldChunkManagerOverworld::class.java.getDeclaredField("h")
    private val iField = WorldChunkManagerOverworld::class.java.getDeclaredField("i")
    private val jField = WorldChunkManagerOverworld::class.java.getDeclaredField("j")
    private val kField = WorldChunkManagerOverworld::class.java.getDeclaredField("k")
	private val biomeMapField = BiomeRegistry::class.java.getDeclaredField("c")
	private val minecraftKeyField = ResourceKey::class.java.getDeclaredField("c")
	private val keyField = MinecraftKey::class.java.getDeclaredField("key")

	private val optionField = WorldChunkManagerMultiNoise::class.java.getDeclaredField("s")
	private val seedFieldMultiNoise = WorldChunkManagerMultiNoise::class.java.getDeclaredField("r")

	init {
		serverWorldsField.isAccessible = true
		worldServerField.isAccessible = true
		chunkProviderServerField.isAccessible = true
		chunkGeneratorField.isAccessible = true
		worldChunkManagerBField.isAccessible = true
		worldChunkManagerCField.isAccessible = true
		hField.isAccessible = true
		iField.isAccessible = true
		jField.isAccessible = true
		kField.isAccessible = true
		biomeMapField.isAccessible = true
		minecraftKeyField.isAccessible = true
		keyField.isAccessible = true
		optionField.isAccessible = true
		seedFieldMultiNoise.isAccessible = true
	}

	private val biomeMap = biomeMapField[null] as Int2ObjectMap<ResourceKey<BiomeBase>>

    fun init(server: Server) {
	    /* replace worlds hashmap on server */
	    serverWorldsField[server] = object : HashMap<String, World>() {
		    override fun put(key: String, value: World): World? {
			    onWorldAdded(value)
			    return super.put(key, value)
		    }
	    }
    }

	fun biomeFromName(name: String?): ResourceKey<BiomeBase>? {
		val lowerName = name?.toLowerCase() ?: return null

		return biomeMap.asIterable().find { key ->
			(keyField[(minecraftKeyField[key.value] as MinecraftKey)] as String) == lowerName
		}?.value
	}

	val lobbyBiomes = listOf(
		Biomes.SNOWY_TUNDRA,
		Biomes.SNOWY_TAIGA,
		Biomes.TAIGA,
		Biomes.PLAINS,
		Biomes.SUNFLOWER_PLAINS,
		Biomes.FOREST,
		Biomes.FLOWER_FOREST,
		Biomes.BIRCH_FOREST,
		Biomes.DARK_FOREST,
		Biomes.JUNGLE,
		Biomes.JUNGLE_EDGE,
		Biomes.BAMBOO_JUNGLE,
		Biomes.DESERT,
		Biomes.SAVANNA,
		Biomes.BADLANDS
	)

	val tallLobbyBiomes = listOf(
		Biomes.MODIFIED_JUNGLE_EDGE,
		Biomes.MODIFIED_BADLANDS_PLATEAU,
		Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU,
		Biomes.MODIFIED_GRAVELLY_MOUNTAINS,
		Biomes.TALL_BIRCH_HILLS,
		Biomes.MUSHROOM_FIELDS,
		Biomes.MODIFIED_JUNGLE,
		Biomes.GIANT_TREE_TAIGA,
		Biomes.WOODED_MOUNTAINS,
		Biomes.ICE_SPIKES,
		Biomes.SNOWY_TAIGA_MOUNTAINS,
		Biomes.ERODED_BADLANDS,
		Biomes.GIANT_SPRUCE_TAIGA
	)

	val lobbyCenterBiomes = arrayOf(
		Biomes.DESERT,
		Biomes.MODIFIED_JUNGLE_EDGE,
		Biomes.PLAINS,
		Biomes.BIRCH_FOREST
	)

	val oceanBiomes = arrayOf(
		Biomes.WARM_OCEAN,
		Biomes.LUKEWARM_OCEAN,
		Biomes.OCEAN
	)

	val pvpBiomes = arrayOf(
		Biomes.SNOWY_TUNDRA,
		Biomes.ICE_SPIKES,
		Biomes.SNOWY_TAIGA,
		Biomes.SNOWY_TAIGA_MOUNTAINS,
		Biomes.MOUNTAINS,
		Biomes.GRAVELLY_MOUNTAINS,
		Biomes.WOODED_MOUNTAINS,
		Biomes.MODIFIED_GRAVELLY_MOUNTAINS,
		Biomes.TAIGA,
		Biomes.TAIGA_MOUNTAINS,
		Biomes.GIANT_TREE_TAIGA,
		Biomes.GIANT_SPRUCE_TAIGA,
		Biomes.PLAINS,
		Biomes.SUNFLOWER_PLAINS,
		Biomes.FOREST,
		Biomes.FLOWER_FOREST,
		Biomes.BIRCH_FOREST,
		Biomes.TALL_BIRCH_FOREST,
		Biomes.DARK_FOREST,
		Biomes.DARK_FOREST_HILLS,
		Biomes.JUNGLE,
		Biomes.MODIFIED_JUNGLE,
		Biomes.MODIFIED_JUNGLE_EDGE,
		Biomes.BAMBOO_JUNGLE,
		Biomes.MUSHROOM_FIELDS,
		Biomes.DESERT,
		Biomes.SAVANNA,
		Biomes.SHATTERED_SAVANNA,
		Biomes.BADLANDS,
		Biomes.ERODED_BADLANDS,
		Biomes.WOODED_BADLANDS_PLATEAU,
		Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU,
		Biomes.BADLANDS_PLATEAU,
		Biomes.SAVANNA_PLATEAU,
		Biomes.MODIFIED_BADLANDS_PLATEAU,
		Biomes.SHATTERED_SAVANNA_PLATEAU,
		Biomes.MOUNTAIN_EDGE,
		Biomes.NETHER_WASTES,
		Biomes.SOUL_SAND_VALLEY,
		Biomes.CRIMSON_FOREST,
		Biomes.WARPED_FOREST,
		Biomes.BASALT_DELTAS
	)

    private fun onWorldAdded(world: World) {
        val worldServer = worldServerField[world] as WorldServer
        val chunkProviderServer = chunkProviderServerField[worldServer] as ChunkProviderServer
        val chunkGenerator = chunkGeneratorField[chunkProviderServer] as ChunkGenerator

        val oldChunkManager = worldChunkManagerBField[chunkGenerator]

	    val (seed, biomeRegistry) = when (oldChunkManager) {
	    	is WorldChunkManagerMultiNoise -> {
			    val optional = optionField[oldChunkManager] as Optional<PairM<IRegistry<BiomeBase>, WorldChunkManagerMultiNoise.b>>
			    Pair(seedFieldMultiNoise.getLong(oldChunkManager), if (optional.isPresent) optional.get().first else null)
	    	}
		    is  WorldChunkManagerOverworld -> Pair(hField.getLong(oldChunkManager), kField[oldChunkManager] as IRegistry<BiomeBase>)
		    else -> Pair(null, null)
	    }

	    /* the old world chunk manager is of a nonsupported type */
	    if (seed == null || biomeRegistry == null) return

	    val customGenerator = when (world.name) {
	    	WorldManager.NETHER_WORLD_NAME -> {
			    WorldChunkManagerNether(seed, biomeRegistry)
	    	}
		    WorldManager.LOBBY_WORLD_NAME -> {
			    WorldChunkManagerOverworldLobby(
				    seed, biomeRegistry,
				    lobbyBiomes.shuffled().zip(tallLobbyBiomes.shuffled()).flatMap { listOf(it.first, it.second) }.take(9),
				    60
			    )
		    }
		    WorldManager.PVP_WORLD_NAME -> {
			    WorldChunkManagerOverworldPvp(seed, biomeRegistry, pvpBiomes)
		    }
		    WorldManager.GAME_WORLD_NAME -> {
			    if (WorldGenOption.getEnabled(WorldGenOption.CHUNK_BIOMES))
				    WorldChunkManagerOverworldChunkBiomes(seed, biomeRegistry)

			    else
				    WorldChunkManagerOverworldGame(
					    seed, biomeRegistry,
					    biomeFromName(WorldGenOption.centerBiome?.name),
					    WorldGenOption.getEnabled(WorldGenOption.MELON_FIX),
					    UHC.startRadius()
				    )
		    }
		    else -> null
	    }

        if (customGenerator != null) {
	        worldChunkManagerBField[chunkGenerator] = customGenerator
	        worldChunkManagerCField[chunkGenerator] = customGenerator
        }
    }
}
