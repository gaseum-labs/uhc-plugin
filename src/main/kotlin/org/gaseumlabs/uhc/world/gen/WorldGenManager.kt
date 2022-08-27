package org.gaseumlabs.uhc.world.gen

import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.reflect.UHCReflect
import org.gaseumlabs.uhc.world.*
import org.gaseumlabs.uhc.world.gen.biomeSource.*
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator
import org.bukkit.*
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
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

		val (newBiomeSource, newSurfaceRule) = when (world.name) {
			WorldManager.GAME_WORLD_NAME -> {
				if (false /* TODO chunk generation options */) {
					BiomeSourceChunkBiomes(
						seed,
						BiomeNo.featureBiomes
					) to UHCSurfaceRule.uhcOverworld()
				} else {
					BiomeSourceGame(
						seed,
						UHC.getConfig().battlegroundRadius,
						BiomeNo.featureBiomes,
						BiomeSourceGame.createAreaGame(seed),
						BiomeSourceGame.createAreaCaves(seed)
					) to UHCSurfaceRule.uhcOverworld()
				}
			}
			WorldManager.NETHER_WORLD_NAME -> {
				return
				//BiomeSourceNether(
				//	seed,
				//	BiomeNo.featureBiomes,
				//	BiomeSourceNether.createAreaNether(seed)
				//) to UHCSurfaceRule.uhcNether()
			}
			WorldManager.LOBBY_WORLD_NAME -> {
				BiomeSourceSingle(seed, BiomeNo.biomes, BiomeNo.BADLANDS) to UHCSurfaceRule.uhcOverworld()
			}
			WorldManager.PVP_WORLD_NAME -> {
				BiomeSourcePvp(seed) to UHCSurfaceRule.uhcOverworld()
			}
			else -> return
		}

		val generator =
			(world as CraftWorld).handle.chunkSource.chunkMap.generator as? NoiseBasedChunkGenerator ?: return

		runtimeBiomeSourceField.set(generator, newBiomeSource)
		biomeSourceField.set(generator, newBiomeSource)

		//TODO manipulate the nether noise
		UHCNoiseGeneratorSettings.inject(
			generator,
			UHCNoiseGeneratorSettings.createGame(false),
			world.name != WorldManager.NETHER_WORLD_NAME,
			newSurfaceRule,
		)
	}
}
