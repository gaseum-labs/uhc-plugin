package com.codeland.uhc.world.gen

import com.codeland.uhc.util.Util
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Server
import org.bukkit.World
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

	    /* replace worlds hashmap on server */
	    serverWorldsField[server] = object : HashMap<String, World>() {
		    override fun put(key: String, value: World): World? {
			    onWorldAdded(value)
			    return super.put(key, value)
		    }
	    }
    }

    private fun onWorldAdded(world: World) {
	    Util.log("WORLD ${world.name} ADDED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        if (world.environment == World.Environment.NORMAL) {
            val worldServer = worldServerField[world] as WorldServer
            val chunkProviderServer = chunkProviderServerField[worldServer] as ChunkProviderServer
            val chunkGenerator = chunkGeneratorField[chunkProviderServer] as ChunkGenerator
            val worldChunkGeneratorOverworld = worldChunkManagerBField[chunkGenerator] as? WorldChunkManagerOverworld ?: return Util.log("Wrong WorldChunkGenerator found.")

            val worldChunkGeneratorOverworldNoOcean = WorldChunkManagerOverworldNoOcean(
                hField.getLong(worldChunkGeneratorOverworld),
                iField.getBoolean(worldChunkGeneratorOverworld),
                jField.getBoolean(worldChunkGeneratorOverworld),
                kField.get(worldChunkGeneratorOverworld) as IRegistry<BiomeBase>
            )

            worldChunkManagerBField[chunkGenerator] = worldChunkGeneratorOverworldNoOcean
	        worldChunkManagerCField[chunkGenerator] = worldChunkGeneratorOverworldNoOcean
        }
    }
}