package org.gaseumlabs.uhc.world.gen

import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.reflect.UHCReflect
import org.gaseumlabs.uhc.world.*
import org.gaseumlabs.uhc.world.WorldGenOption.AMPLIFIED
import org.gaseumlabs.uhc.world.gen.biomeSource.*
import org.gaseumlabs.uhc.world.gen.climate.UHCNoiseGeneratorSettings
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import org.bukkit.*
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import kotlin.Pair
import kotlin.collections.HashMap

object WorldGenManager {
	private val serverWorldsField = UHCReflect<CraftServer, Map<String, World>>(CraftServer::class, "worlds")

	private val biomeSourceField = UHCReflect<ChunkGenerator, BiomeSource>(ChunkGenerator::class, "biomeSource")
	private val runtimeBiomeSourceField =
		UHCReflect<ChunkGenerator, BiomeSource>(ChunkGenerator::class, "runtimeBiomeSource")

	/* fields of MuliNoiseBiomeSource */
	private val parametersField = UHCReflect<MultiNoiseBiomeSource, Climate.ParameterList<Holder<Biome>>>(
		MultiNoiseBiomeSource::class,
		"parameters"
	)

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
		BiomeNo.delayedInit()

		if (world.name == WorldManager.BAD_NETHER_WORLD_NAME || world.name == WorldManager.END_WORLD_NAME) return

		val generator =
			(world as CraftWorld).handle.chunkSource.chunkMap.generator as? NoiseBasedChunkGenerator ?: return

		val seed = world.seed

		val (biomeManager, featureManager) = when (world.name) {
			WorldManager.GAME_WORLD_NAME -> {
				if (UHC.getConfig().worldGenEnabled(WorldGenOption.CHUNK_BIOMES)) {
					Pair(BiomeSourceChunkBiomes(seed), null)

				} else {
					BiomeSourceGame.createFeaturesPair(
						seed,
						BiomeNo.fromName(UHC.getConfig().centerBiome.get()?.name),
						UHC.getConfig().endgameRadius.get(),
					)
				}
			}
			WorldManager.NETHER_WORLD_NAME -> {
				BiomeSourceNether.createFeaturesPair(seed)
			}
			WorldManager.LOBBY_WORLD_NAME -> {
				//BiomeSourceSingle(seed, BiomeNo.BADLANDS) to null
				//BiomeSourcePvp(seed) to null
				BiomeSourceGame.createFeaturesPair(
					seed,
					BiomeNo.fromName(UHC.getConfig().centerBiome.get()?.name),
					UHC.getConfig().endgameRadius.get(),
				)
			}
			WorldManager.PVP_WORLD_NAME -> {
				BiomeSourcePvp(seed) to null
			}
			else -> Pair(null, null)
		}

		if (biomeManager != null) {
			runtimeBiomeSourceField.set(generator, biomeManager)
			biomeSourceField.set(generator, featureManager ?: biomeManager)

			UHCNoiseGeneratorSettings.inject(
				generator,
				seed,
				UHCNoiseGeneratorSettings.createGame(UHC.getConfig().worldGenEnabled(AMPLIFIED))
			)
		}
	}
}
