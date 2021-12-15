package com.codeland.uhc.world.gen

import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.world.WorldGenOption
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.gen.chunkManager.*
import net.minecraft.core.IRegistry
import net.minecraft.server.level.ChunkProviderServer
import net.minecraft.server.level.WorldServer
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract
import net.minecraft.world.level.levelgen.GeneratorSettingBase
import org.bukkit.*
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import java.util.*
import java.util.function.*
import kotlin.Pair
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
				if (key == WorldManager.END_WORLD_NAME || key == WorldManager.BAD_NETHER_WORLD_NAME) {
					SchedulerUtil.nextTick { WorldManager.destroyWorld(key) }
				}
				onWorldAdded(value)
				return super.put(key, value)
			}
		}
	}

	private fun onWorldAdded(world: World) {
		if (world.name == WorldManager.BAD_NETHER_WORLD_NAME || world.name == WorldManager.END_WORLD_NAME) return

		val worldServer = worldServerField[world] as WorldServer
		val chunkProviderServer = chunkProviderServerField[worldServer] as ChunkProviderServer
		val chunkGenerator = chunkGeneratorField[chunkProviderServer] as ChunkGenerator

		/* grab the existing chunk manager */
		val oldChunkManager = worldChunkManagerBField[chunkGenerator]
		val (seed, biomeRegistry) = when (oldChunkManager) {
			is WorldChunkManagerMultiNoise -> {
				val optional =
					optionField[oldChunkManager] as Optional<PairM<IRegistry<BiomeBase>, WorldChunkManagerMultiNoise.b>>
				Pair(seedFieldMultiNoise.getLong(oldChunkManager),
					if (optional.isPresent) optional.get().first else null)
			}
			is WorldChunkManagerOverworld -> Pair(hField.getLong(oldChunkManager),
				kField[oldChunkManager] as IRegistry<BiomeBase>)
			else -> Pair(null, null)
		}

		/* the old world chunk manager is of a nonsupported type */
		if (seed == null || biomeRegistry == null) return

		val (biomeManager, featureManager) = when (world.name) {
			WorldManager.GAME_WORLD_NAME -> {
				if (UHC.getConfig().worldGenEnabled(WorldGenOption.CHUNK_BIOMES)) {
					Pair(WorldChunkManagerOverworldChunkBiomes(seed, biomeRegistry), null)

				} else {
					val manager = WorldChunkManagerOverworldGame(
						seed, biomeRegistry,
						BiomeNo.fromName(UHC.getConfig().centerBiome.get()?.name),
						UHC.getConfig().endgameRadius.get(),
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
				Pair(WorldChunkManagerOverworldGame(
					seed, biomeRegistry,
					null, 0, false
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
				noodleCavesField[customGeneratorSettings] = true
				aquifersField[customGeneratorSettings] = true

				gField[chunkGenerator] = Supplier<GeneratorSettingBase> { customGeneratorSettings }
			}

			if (world.name != WorldManager.NETHER_WORLD_NAME) NoiseSamplerUHC.inject(
				chunkGenerator as ChunkGeneratorAbstract,
				biomeManager,
				if (world.name == WorldManager.GAME_WORLD_NAME) {
					UHC.getConfig().worldGenEnabled(WorldGenOption.AMPLIFIED)
				} else {
					false
				},
				world.name == WorldManager.PVP_WORLD_NAME,
				if (world.name == WorldManager.LOBBY_WORLD_NAME) Lobby.LOBBY_RADIUS / 2 else UHC.getConfig().endgameRadius.get()
			)

			worldChunkManagerBField[chunkGenerator] = featureManager ?: biomeManager
			worldChunkManagerCField[chunkGenerator] = biomeManager
		}
	}
}
