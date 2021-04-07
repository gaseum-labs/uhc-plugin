package com.codeland.uhc.world.gen

import com.codeland.uhc.util.Util.log
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.event.Listener
import kotlin.collections.HashMap

class WorldGenManager(private val server: Server) : Listener {

    private val serverWorldsField = CraftServer::class.java.getDeclaredField("worlds")

    private val worldServerField = CraftWorld::class.java.getDeclaredField("world")
    private val chunkProviderServerField = WorldServer::class.java.getDeclaredField("chunkProvider")
    private val chunkGeneratorField = ChunkProviderServer::class.java.getDeclaredField("chunkGenerator")
    private val worldChunkManagerField = ChunkGenerator::class.java.getDeclaredField("b")
    private val hField = WorldChunkManagerOverworld::class.java.getDeclaredField("h")
    private val iField = WorldChunkManagerOverworld::class.java.getDeclaredField("i")
    private val jField = WorldChunkManagerOverworld::class.java.getDeclaredField("j")
    private val kField = WorldChunkManagerOverworld::class.java.getDeclaredField("k")

    init {
        if (serverWorldsField.trySetAccessible()) {
            val wrapper = object: HashMap<String, World>() {
                override fun put(key: String, value: World): World? {
                    onWorldAdded(value)
                    return super.put(key, value)
                }
            }

            serverWorldsField[server] = wrapper
        } else {
            throw RuntimeException("Failed to access CraftServer.worlds")
        }

        worldServerField.isAccessible = true
        chunkProviderServerField.isAccessible = true
        chunkGeneratorField.isAccessible = true
        worldChunkManagerField.isAccessible = true
        hField.isAccessible = true
        iField.isAccessible = true
        jField.isAccessible = true
        kField.isAccessible = true
    }

    private fun onWorldAdded(world: World) {
        log("WORLD ${world.name} ADDED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        if (world.environment == World.Environment.NORMAL) {
            val worldServer = worldServerField.get(world as CraftWorld) as WorldServer
            val chunkProviderServer = chunkProviderServerField.get(worldServer) as ChunkProviderServer
            val chunkGenerator = chunkGeneratorField.get(chunkProviderServer) as ChunkGenerator
            val worldChunkGeneratorOverworld = worldChunkManagerField.get(chunkGenerator) as? WorldChunkManagerOverworld ?: return log("Wrong WorldChunkGenerator found.")
            val worldChunkGeneratorOverworldNoOcean = WorldChunkManagerOverworldNoOcean(
                hField.getLong(worldChunkGeneratorOverworld),
                iField.getBoolean(worldChunkGeneratorOverworld),
                jField.getBoolean(worldChunkGeneratorOverworld),
                kField.get(worldChunkGeneratorOverworld) as IRegistry<BiomeBase>
            )
            worldChunkManagerField.set(chunkGenerator, worldChunkGeneratorOverworldNoOcean)
        }
    }
}