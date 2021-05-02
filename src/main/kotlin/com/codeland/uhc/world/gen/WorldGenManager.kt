package com.codeland.uhc.world.gen

import com.codeland.uhc.core.WorldGenOption
import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.util.Util
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld

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

	var centerBiome: ResourceKey<BiomeBase>? = null

    fun init(server: Server) {
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

	    /* replace worlds hashmap on server */

	    serverWorldsField[server] = object : HashMap<String, World>() {
		    override fun put(key: String, value: World): World? {
			    onWorldAdded(value)
			    return super.put(key, value)
		    }
	    }

	    /* parse center biome */

	    val biomeMap = biomeMapField[null] as Int2ObjectMap<ResourceKey<BiomeBase>>
	    centerBiome = biomeMap.asIterable().find { key ->
		    (keyField[(minecraftKeyField[key.value] as MinecraftKey)] as String).toLowerCase() == WorldGenOption.CENTER_BIOME.get()
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

	fun getBiomes(): Pair<ResourceKey<BiomeBase>, ResourceKey<BiomeBase>> {
		val list = lobbyBiomes.asIterable().shuffled()
		return Pair(list[0], list[1])
	}

    private fun onWorldAdded(world: World) {
        if (world.environment == World.Environment.NORMAL) {
            val worldServer = worldServerField[world] as WorldServer
            val chunkProviderServer = chunkProviderServerField[worldServer] as ChunkProviderServer
            val chunkGenerator = chunkGeneratorField[chunkProviderServer] as ChunkGenerator
            val worldChunkGeneratorOverworld = worldChunkManagerBField[chunkGenerator] as? WorldChunkManagerOverworld ?: return Util.log("Wrong WorldChunkGenerator found.")

	        val customWorldChunkGeneratorOverworld = when (world.name) {
		        /* lobby world */
		        WorldManager.LOBBY_WORLD_NAME -> {
			        val (biome0, biome1) = getBiomes()
			        WorldChunkManagerOverworldLobby(
				        hField.getLong(worldChunkGeneratorOverworld),
				        iField.getBoolean(worldChunkGeneratorOverworld),
				        jField.getBoolean(worldChunkGeneratorOverworld),
				        kField.get(worldChunkGeneratorOverworld) as IRegistry<BiomeBase>,
				        biome0,
				        biome1,
				        Util.randFromArray(lobbyCenterBiomes),
				        Util.randFromArray(oceanBiomes),
				        60
			        )
		        }
		        /* pvp world */
		        WorldManager.PVP_WORLD_NAME -> {
			        WorldChunkManagerOverworldPvp(
				        hField.getLong(worldChunkGeneratorOverworld),
				        iField.getBoolean(worldChunkGeneratorOverworld),
				        jField.getBoolean(worldChunkGeneratorOverworld),
				        kField.get(worldChunkGeneratorOverworld) as IRegistry<BiomeBase>,
				        pvpBiomes
			        )
		        }
		        /* game world */
		        else -> {
			        WorldChunkManagerOverworldNoOcean(
				        hField.getLong(worldChunkGeneratorOverworld),
				        iField.getBoolean(worldChunkGeneratorOverworld),
				        jField.getBoolean(worldChunkGeneratorOverworld),
				        kField.get(worldChunkGeneratorOverworld) as IRegistry<BiomeBase>,
				        centerBiome
			        )
		        }
	        }

	        worldChunkManagerBField[chunkGenerator] = customWorldChunkGeneratorOverworld
	        worldChunkManagerCField[chunkGenerator] = customWorldChunkGeneratorOverworld
        }
    }
}