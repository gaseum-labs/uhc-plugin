package com.codeland.uhc.world.gen

import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.world.*
import com.codeland.uhc.world.gen.chunkManager.*
import net.minecraft.server.level.*
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.chunk.ChunkGenerator
import org.bukkit.*
import org.bukkit.craftbukkit.v1_18_R1.CraftServer
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld
import java.util.*
import java.util.function.*
import kotlin.Pair

object WorldGenManager {
	private val serverWorldsField = UHCReflect<CraftServer, Map<String, World>>(CraftServer::class, "worlds")

	private val biomeSourceField = UHCReflect<ChunkGenerator, BiomeSource>(ChunkGenerator::class, "biomeSource")
	private val runtimeBiomeSourceField =
		UHCReflect<ChunkGenerator, BiomeSource>(ChunkGenerator::class, "runtimeBiomeSource")

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
	private val noChunkMobsField = GeneratorSettingBase::class.java.getDeclaredField("q")

	init {
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
		xField.isAccessible = true
		noiseCavesField.isAccessible = true
		noodleCavesField.isAccessible = true
		aquifersField.isAccessible = true
		noChunkMobsField.isAccessible = true
	}

	fun init(server: Server) {
		/* replace worlds hashmap on server */
		serverWorldsField.set(server as CraftServer, object : HashMap<String, World>() {
			override fun put(key: String, value: World): World? {
				if (key == WorldManager.END_WORLD_NAME || key == WorldManager.BAD_NETHER_WORLD_NAME) {
					SchedulerUtil.nextTick { WorldManager.destroyWorld(key) }
				}
				onWorldAdded(value)
				return super.put(key, value)
			}
		})
	}

	private fun onWorldAdded(world: World) {
		if (world.name == WorldManager.BAD_NETHER_WORLD_NAME || world.name == WorldManager.END_WORLD_NAME) return

		val generator = (world as CraftWorld).handle.chunkSource.chunkMap.generator

		/* grab the existing chunk manager */
		val oldBiomeSource = biomeSourceField.get(generator)

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
				Pair(WorldChunkManagerOverworldSingleBiome(
					seed, biomeRegistry, BiomeNo.MODIFIED_BADLANDS_PLATEAU
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
				noChunkMobsField[customGeneratorSettings] = true

				gField[chunkGenerator] = Supplier<GeneratorSettingBase> { customGeneratorSettings }
			}

			if (world.name != WorldManager.NETHER_WORLD_NAME) NoiseSamplerUHC.inject(
				chunkGenerator,
				biomeManager,
				if (world.name == WorldManager.GAME_WORLD_NAME) {
					UHC.getConfig().worldGenEnabled(WorldGenOption.AMPLIFIED)
				} else {
					false
				},
				world.name == WorldManager.PVP_WORLD_NAME,
				if (world.name == WorldManager.LOBBY_WORLD_NAME) 0 else UHC.getConfig().endgameRadius.get()
			)

			worldChunkManagerBField[chunkGenerator] = featureManager ?: biomeManager
			worldChunkManagerCField[chunkGenerator] = biomeManager
		}
	}
}
