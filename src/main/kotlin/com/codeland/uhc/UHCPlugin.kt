package com.codeland.uhc

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.command.AdminCommands
import com.codeland.uhc.command.ParticipantCommands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.WaitingEventListener
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType


class UHCPlugin : JavaPlugin() {

	private val commandManager: PaperCommandManager by lazy { PaperCommandManager(this) }

	override fun onEnable() {
		commandManager.registerCommand(AdminCommands())
		commandManager.registerCommand(ParticipantCommands())

		server.pluginManager.registerEvents(WaitingEventListener(), this)
		server.pluginManager.registerEvents(GameRunner.gui, this)

		GameRunner.plugin = this

		server.scheduler.scheduleSyncDelayedTask(this, Runnable {
			if (server.scoreboardManager.mainScoreboard.getObjective("hp") == null) {
				server.scoreboardManager.mainScoreboard.registerNewObjective("hp", "health", "hp", RenderType.HEARTS)
			}
			server.scoreboardManager.mainScoreboard.getObjective("hp")!!.displaySlot = DisplaySlot.PLAYER_LIST
		})

		//server.pluginManager.registerEvents(WorldGenListener(), this)
	}

	override fun onDisable() {}

	/*override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator? {
		if (worldName == "uhc_world") {
			if (id == "no_structures") {
				return WorldGeneratorApi
						.getInstance(this, 0, 6)
						.createCustomGenerator(WorldRef.ofName(worldName)) {
							it.baseTerrainGenerator = UHCTerrainGenerator()
							this.logger.info("Enabled the UHC world generator for world \"$worldName\"")
						}
			}
		}
		return VanillaChunkGenerator()
	}*/
}

/*
class TestWorldCreator : WorldCreator("uhc_world") {
	override fun generator(): ChunkGenerator? {
		return TestChunkGenerator()
	}

	override fun generateStructures() = false
}

class TestChunkGenerator : ChunkGenerator() {
	private val oceanMap = mapOf(
			WARM_OCEAN to BADLANDS,

			LUKEWARM_OCEAN to DARK_FOREST,
			DEEP_LUKEWARM_OCEAN to JUNGLE,

			OCEAN to PLAINS,
			DEEP_OCEAN to FOREST,

			COLD_OCEAN to MOUNTAINS,
			DEEP_COLD_OCEAN to TAIGA,

			FROZEN_OCEAN to SNOWY_TUNDRA,
			DEEP_FROZEN_OCEAN to SNOWY_TAIGA
	)

	private var vanillaGenerator: ChunkGenerator? = null

	override fun shouldGenerateStructures() = false

	override fun generateChunkData(world: World, random: Random, x: Int, z: Int, biome: BiomeGrid): ChunkData {
		val currentBiome = biome.getBiome(x, 0, z);
		println("BIOME: $currentBiome")
		if (oceanMap.containsKey(currentBiome))
			biome.setBiome(x, 0, z, oceanMap[currentBiome]!!)

		val worldServer = (world as CraftWorld).handle

		//var cpg = ChunkProviderGenerate(world as GeneratorAccess, worldServer.chunkProvider.chunkGenerator.worldChunkManager, GeneratorSettingsOverworld())

		//return cpg.generateChunkData(world, random, x, z, biome)

		return super.generateChunkData(world, random, x, z, biome)
	}
}

class WorldGenListener : Listener {
	@EventHandler
	fun onWorldInit(event: WorldInitEvent) {
		println("WORLD INIT: ${event.world.name}")

		println("DEFAULT WORLD GENERATOR: ${(event.world as CraftWorld).handle.chunkProvider.chunkGenerator}")
   val serverWorld = (event.world as CraftWorld).handle
		val chunkProvider = serverWorld.chunkProvider
		val chunkGenerator = TestChunkGenerator()



		val chunkGeneratorField = ChunkProviderServer::class.java.getDeclaredField("chunkGenerator")
		chunkGeneratorField.isAccessible = true
		chunkGeneratorField.set(chunkProvider, chunkGenerator)
		val playerChunkMapField = ChunkProviderServer::class.java.getDeclaredField("playerChunkMap")
		playerChunkMapField.isAccessible = true
		playerChunkMapField.set(chunkProvider.playerChunkMap, chunkGenerator)

	}

	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent) {
		if (event.isNewChunk)
			println("CHUNK LOAD: ${event.chunk.x}, ${event.chunk.z}")
	}
}
*/
