package org.gaseumlabs.uhc.world

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.bukkit.*
import org.bukkit.World.Environment
import org.bukkit.entity.SpawnCategory.ANIMAL
import org.bukkit.entity.SpawnCategory.MONSTER
import org.bukkit.persistence.PersistentDataHolder
import org.gaseumlabs.uhc.core.Lobby
import org.gaseumlabs.uhc.core.UHC
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

	fun init() {
		pvpWorld = loadWorld(PVP_WORLD_NAME, Environment.NORMAL, true)
		lobbyWorld = loadWorld(LOBBY_WORLD_NAME, Environment.NORMAL, true)
		prepareWorld(lobbyWorld)
	}

	fun refreshGameWorlds() {
		gameWorld = refreshWorld(GAME_WORLD_NAME, Environment.NORMAL, false)
		netherWorld = refreshWorld(NETHER_WORLD_NAME, Environment.NETHER, false)
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
		world.setSpawnLimit(MONSTER, 0)
		world.setSpawnLimit(ANIMAL, 0)

		if (world.name == LOBBY_WORLD_NAME || world.name == PVP_WORLD_NAME) {
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)

			if (world.name == PVP_WORLD_NAME) {
				world.time = 6000
				world.isThundering = false
				world.setStorm(false)
				ArenaManager.loadWorldInfo(world)
			}
		}
	}

	fun destroyWorld(name: String) {
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

			/* remove players from this world */
			Bukkit.getOnlinePlayers().forEach { player ->
				if (oldWorld === player.world) Lobby.onSpawnLobby(player)
			}

			Bukkit.getServer().unloadWorld(oldWorld, false)
		}

		val file = File(name)
		if (file.exists() && file.isDirectory) file.deleteRecursively()
	}

	fun loadWorld(name: String, environment: Environment, structures: Boolean): World {
		val existing = Bukkit.getWorld(name)
		if (existing != null) return existing

		val creator = WorldCreator(name).environment(environment).generateStructures(structures)

		val world = creator.createWorld() ?: throw Error("Could not create world \"${name}\"")
		prepareWorld(world)
		return world
	}

	fun refreshWorld(name: String, environment: Environment, structures: Boolean): World {
		destroyWorld(name)
		return loadWorld(name, environment, structures)
	}

	fun getGameWorldsBy(environment: Environment) = if (environment === Environment.NORMAL) {
		(gameWorld ?: throw Error("no game world")) to (netherWorld ?: throw Error("no nether world"))
	} else {
		(netherWorld ?: throw Error("no nether world")) to (gameWorld ?: throw Error("no game world"))
	}
}
