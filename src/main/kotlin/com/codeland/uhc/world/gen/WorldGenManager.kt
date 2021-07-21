package com.codeland.uhc.world.gen

import com.codeland.uhc.core.UHC
import com.codeland.uhc.world.WorldGenOption
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.gen.chunkManager.*
import net.minecraft.core.IRegistry
import net.minecraft.server.level.ChunkProviderServer
import net.minecraft.server.level.WorldServer
import net.minecraft.util.MathHelper
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.WorldChunkManagerMultiNoise
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract
import net.minecraft.world.level.levelgen.GeneratorSettingBase
import net.minecraft.world.level.levelgen.NoodleCavifier
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import java.util.*
import java.util.function.Supplier
import kotlin.Double
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.math.abs
import com.mojang.datafixers.util.Pair as PairM

object WorldGenManager {
    private val serverWorldsField = CraftServer::class.java.getDeclaredField("worlds")
    private val worldServerField = CraftWorld::class.java.getDeclaredField("world")
    private val chunkProviderServerField = WorldServer::class.java.getDeclaredField("C")
    private val chunkGeneratorField = ChunkProviderServer::class.java.getDeclaredField("d")
    private val worldChunkManagerBField = ChunkGenerator::class.java.getDeclaredField("b")
	private val worldChunkManagerCField = ChunkGenerator::class.java.getDeclaredField("c")
    private val hField = WorldChunkManagerOverworld::class.java.getDeclaredField("h")
    private val iField = WorldChunkManagerOverworld::class.java.getDeclaredField("i")
    private val jField = WorldChunkManagerOverworld::class.java.getDeclaredField("j")
    private val kField = WorldChunkManagerOverworld::class.java.getDeclaredField("k")

	private val optionField = WorldChunkManagerMultiNoise::class.java.getDeclaredField("s")
	private val seedFieldMultiNoise = WorldChunkManagerMultiNoise::class.java.getDeclaredField("r")

	private val gField = ChunkGeneratorAbstract::class.java.getDeclaredField("g")
	private val xField = ChunkGeneratorAbstract::class.java.getDeclaredField("x")

	private val noiseCavesField = GeneratorSettingBase::class.java.getDeclaredField("s")
	private val noodleCavesField = GeneratorSettingBase::class.java.getDeclaredField("u")
	private val aquifersField = GeneratorSettingBase::class.java.getDeclaredField("r")

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
		optionField.isAccessible = true
		seedFieldMultiNoise.isAccessible = true
		gField.isAccessible = true
		noiseCavesField.isAccessible = true
		noodleCavesField.isAccessible = true
		aquifersField.isAccessible = true
		xField.isAccessible = true
	}

    fun init(server: Server) {
	    /* replace worlds hashmap on server */
	    serverWorldsField[server] = object : HashMap<String, World>() {
		    override fun put(key: String, value: World): World? {
			    onWorldAdded(value)
			    return super.put(key, value)
		    }
	    }
    }

	val lobbyBiomes = listOf(
		Biomes.m,
		Biomes.E,
		Biomes.f,
		Biomes.b,
		Biomes.aa,
		Biomes.e,
		Biomes.ad,
		Biomes.B,
		Biomes.D,
		Biomes.v,
		Biomes.x,
		Biomes.av,
		Biomes.c,
		Biomes.J,
		Biomes.L
	)

	val tallLobbyBiomes = listOf(
		Biomes.ai,
		Biomes.au,
		Biomes.at,
		Biomes.ap,
		Biomes.ak,
		Biomes.o,
		Biomes.ah,
		Biomes.G,
		Biomes.I,
		Biomes.ag,
		Biomes.am,
		Biomes.`as`,
		Biomes.an
	)

    private fun onWorldAdded(world: World) {
        val worldServer = worldServerField[world] as WorldServer
        val chunkProviderServer = chunkProviderServerField[worldServer] as ChunkProviderServer
        val chunkGenerator = chunkGeneratorField[chunkProviderServer] as ChunkGenerator

	    /* grab the existing chunk manager */
        val oldChunkManager = worldChunkManagerBField[chunkGenerator]
	    val (seed, biomeRegistry) = when (oldChunkManager) {
	    	is WorldChunkManagerMultiNoise -> {
			    val optional = optionField[oldChunkManager] as Optional<PairM<IRegistry<BiomeBase>, WorldChunkManagerMultiNoise.b>>
			    Pair(seedFieldMultiNoise.getLong(oldChunkManager), if (optional.isPresent) optional.get().first else null)
	    	}
		    is WorldChunkManagerOverworld -> Pair(hField.getLong(oldChunkManager), kField[oldChunkManager] as IRegistry<BiomeBase>)
		    else -> Pair(null, null)
	    }

	    /* the old world chunk manager is of a nonsupported type */
	    if (seed == null || biomeRegistry == null) return

	    val (biomeManager, featureManager) = when (world.name) {
	        WorldManager.GAME_WORLD_NAME -> {
			    if (WorldGenOption.getEnabled(WorldGenOption.CHUNK_BIOMES)) {
				    Pair(WorldChunkManagerOverworldChunkBiomes(seed, biomeRegistry), null)

			    } else {
			    	val manager = WorldChunkManagerOverworldGame(
					    seed, biomeRegistry,
					    BiomeNo.fromName(WorldGenOption.centerBiome?.name),
					    WorldGenOption.getEnabled(WorldGenOption.MELON_FIX),
					    UHC.startRadius(),
					    UHC.endRadius(),
					    false
				    )
				    Pair(manager, manager.withFeatures(true))
			    }
	        }
		    WorldManager.NETHER_WORLD_NAME -> {
			    val manager = WorldChunkManagerNether(seed, biomeRegistry, false)
			    Pair(manager, manager.withFeatures(true))
		    }
		    WorldManager.LOBBY_WORLD_NAME -> {
			    Pair(WorldChunkManagerOverworldLobby(
				    seed, biomeRegistry,
				    lobbyBiomes.shuffled().zip(tallLobbyBiomes.shuffled()).flatMap { listOf(it.first, it.second) }.take(9),
				    60
			    ), null)
		    }
		    WorldManager.PVP_WORLD_NAME -> {
			    Pair(WorldChunkManagerOverworldPvp(seed, biomeRegistry), null)
		    }
		    else -> Pair(null, null)
	    }

	    if (biomeManager != null) {
	    	/* aquifers and noise caves in the game world */
	    	if (world.name == WorldManager.GAME_WORLD_NAME) {
			    val customGeneratorSettings = (gField[chunkGenerator] as Supplier<GeneratorSettingBase>).get()

			    noiseCavesField[customGeneratorSettings] = true
			    aquifersField[customGeneratorSettings] = true

			    gField[chunkGenerator] = Supplier<GeneratorSettingBase> { customGeneratorSettings }
		    }

		    NoiseSamplerUHC.inject(
			    chunkGenerator as ChunkGeneratorAbstract,
			    biomeManager,
			    if (world.name == WorldManager.GAME_WORLD_NAME) {
				    WorldGenOption.getEnabled(WorldGenOption.AMPLIFIED)
			    } else {
				    false
			    },
			    world.name == WorldManager.PVP_WORLD_NAME
		    )

		    worldChunkManagerBField[chunkGenerator] = featureManager ?: biomeManager
		    worldChunkManagerCField[chunkGenerator] = biomeManager
	    }
    }
}
