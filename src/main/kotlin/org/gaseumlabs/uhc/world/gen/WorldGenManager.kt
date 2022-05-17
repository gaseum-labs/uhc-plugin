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
		BiomeNo
		CustomNoise

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
		val seed = world.seed

		val newBiomeSource = when (world.name) {
			WorldManager.GAME_WORLD_NAME -> {
				if (UHC.getConfig().worldGenEnabled(WorldGenOption.CHUNK_BIOMES)) {
					BiomeSourceChunkBiomes(
						seed,
						BiomeNo.featureBiomes
					)
				} else {
					BiomeSourceGame(
						seed,
						BiomeNo.fromName(UHC.getConfig().centerBiome.get()?.name),
						UHC.getConfig().endgameRadius.get(),
						BiomeNo.featureBiomes,
						BiomeSourceGame.createAreaGame(seed)
					)
				}
			}
			WorldManager.NETHER_WORLD_NAME -> {
				BiomeSourceNether(
					seed,
					BiomeNo.featureBiomes,
					BiomeSourceNether.createAreaNether(seed)
				)
			}
			WorldManager.LOBBY_WORLD_NAME -> {
				BiomeSourceSingle(seed, BiomeNo.biomes, BiomeNo.BADLANDS)
			}
			WorldManager.PVP_WORLD_NAME -> {
				BiomeSourcePvp(seed)
			}
			else -> return
		}

		val generator =
			(world as CraftWorld).handle.chunkSource.chunkMap.generator as? NoiseBasedChunkGenerator ?: return

		runtimeBiomeSourceField.set(generator, newBiomeSource)
		biomeSourceField.set(generator, newBiomeSource)

		//TODO manipulate the nether noise
		if (world.name != WorldManager.NETHER_WORLD_NAME) {
			UHCNoiseGeneratorSettings.inject(
				generator,
				seed,
				UHCNoiseGeneratorSettings.createGame(UHC.getConfig().worldGenEnabled(AMPLIFIED))
			)
		}
	}
}
