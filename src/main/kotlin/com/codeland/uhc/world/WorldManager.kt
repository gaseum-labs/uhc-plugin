package com.codeland.uhc.world

import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.world.chunkPlacer.ChunkPlacerHolder
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.*
import java.io.File

object WorldManager {
	const val LOBBY_WORLD_NAME = "world"
	const val PVP_WORLD_NAME = "uhc_pvp"
	const val GAME_WORLD_NAME = "uhc_game"
	const val NETHER_WORLD_NAME = "uhc_nether"

	const val BAD_NETHER_WORLD_NAME = "world_nether"
	const val END_WORLD_NAME = "world_the_end"

	/* cached worlds */

	lateinit var lobbyWorld: World
		private set
	lateinit var pvpWorld: World
		private set
	var gameWorld: World? = null
		private set
	var netherWorld: World? = null
		private set

	/* */

	fun init(): String? {
		pvpWorld = recoverWorld(PVP_WORLD_NAME, World.Environment.NORMAL, true)
			?: return "PVP world could not be loaded"

		Bukkit.unloadWorld(END_WORLD_NAME, false)
		Bukkit.unloadWorld(BAD_NETHER_WORLD_NAME, false)
		Bukkit.unloadWorld(GAME_WORLD_NAME, true)
		Bukkit.unloadWorld(NETHER_WORLD_NAME, true)

		lobbyWorld = Bukkit.getWorld(LOBBY_WORLD_NAME)
			?: return "Lobby world could not be loaded"

		prepareWorld(lobbyWorld)

		return null
	}

	fun refreshGameWorlds() {
		gameWorld = refreshWorld(GAME_WORLD_NAME, World.Environment.NORMAL, false)
		netherWorld = refreshWorld(NETHER_WORLD_NAME, World.Environment.NETHER, false)
	}

	fun destroyGameWorlds() {
		destroyWorld(GAME_WORLD_NAME)
		destroyWorld(NETHER_WORLD_NAME)

		gameWorld = null
		netherWorld = null
	}

	fun isNonGameWorld(world: World): Boolean {
		return world === lobbyWorld || world === pvpWorld
	}

	fun isGameWorld(world: World): Boolean {
		return world === gameWorld || world === netherWorld
	}

	fun existsUnloaded(name: String): Boolean {
		val worldFolder = File(name)
		return worldFolder.exists() && worldFolder.isDirectory
	}

	fun prepareWorld(world: World) {
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true)
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
		world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
		world.difficulty = Difficulty.NORMAL
		world.animalSpawnLimit = 0
		world.monsterSpawnLimit = 0

		if (world.name == GAME_WORLD_NAME || world.name == NETHER_WORLD_NAME) {
			ChunkPlacerHolder.values().forEach { (chunkPlacer) ->
				if (chunkPlacer is DelayedChunkPlacer) {
					chunkPlacer.clean()
				}
			}
		}

		if (world.name == LOBBY_WORLD_NAME || world.name == PVP_WORLD_NAME) {
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)

			world.time = 6000
			world.isThundering = false
			world.setStorm(false)

			if (world.name == LOBBY_WORLD_NAME) {
				world.worldBorder.center = Location(world, 0.5, 0.0, 0.5)
				world.worldBorder.size = Lobby.LOBBY_RADIUS * 2 + 1.0

			} else if (world.name == PVP_WORLD_NAME) {
				ArenaManager.loadWorldInfo(world)
			}
		}
	}

	fun destroyWorld(name: String): World? {
		val oldWorld = Bukkit.getServer().getWorld(name)

		if (oldWorld != null) {
			/* remove offline zombies from this world */
			PlayerData.playerDataList.forEach { (_, playerData) ->
				val zombie = playerData.offlineZombie

				if (zombie != null && zombie.world === oldWorld) {
					zombie.remove()
					playerData.offlineZombie = null
				}
			}

			Bukkit.getServer().unloadWorld(oldWorld, false)
		}

		val file = File(name)
		if (file.exists() && file.isDirectory) file.deleteRecursively()

		return oldWorld
	}

	fun refreshWorld(name: String, environment: World.Environment, structures: Boolean): World? {
		destroyWorld(name)

		val creator = WorldCreator(name).environment(environment).generateStructures(structures)

		val world = creator.createWorld()

		if (world != null) prepareWorld(world)

		return world
	}

	fun recoverWorld(name: String, environment: World.Environment, structures: Boolean): World? {
		return if (existsUnloaded(name)) {
			val world = WorldCreator(name).createWorld()

			if (world != null) prepareWorld(world)

			world

		} else {
			refreshWorld(name, environment, structures)
		}
	}
}
