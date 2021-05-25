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

	val lobbyBiomes = arrayOf(
		Biomes.MODIFIED_JUNGLE,
		Biomes.MUSHROOM_FIELDS,
		Biomes.SHATTERED_SAVANNA,
		Biomes.ERODED_BADLANDS,
		Biomes.GIANT_SPRUCE_TAIGA_HILLS,
		Biomes.DARK_FOREST_HILLS,
		Biomes.ICE_SPIKES,
		Biomes.BASALT_DELTAS
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
		Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU,
		Biomes.MODIFIED_JUNGLE_EDGE,
		Biomes.ICE_SPIKES,
		Biomes.DESERT_HILLS,
		Biomes.JUNGLE,
		Biomes.DARK_FOREST_HILLS,
		Biomes.TALL_BIRCH_FOREST,
		Biomes.SUNFLOWER_PLAINS,
		Biomes.GRAVELLY_MOUNTAINS,
		Biomes.SNOWY_TUNDRA
	)

	private fun getLobbyBiomes(): Pair<ResourceKey<BiomeBase>, ResourceKey<BiomeBase>> {
		val list = lobbyBiomes.asIterable().shuffled()
		return Pair(list[0], list[1])
	}

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
			    val (biome0, biome1) = getLobbyBiomes()
			    WorldChunkManagerOverworldLobby(
				    seed, biomeRegistry,
				    biome0, biome1,
				    Util.randFromArray(lobbyCenterBiomes),
				    Util.randFromArray(oceanBiomes),
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
					    UHC.startRadius
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
