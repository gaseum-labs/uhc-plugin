package com.codeland.uhc.world

import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolderType
import org.bukkit.*
import org.bukkit.block.Biome
import java.io.File

object WorldManager {
	const val LOBBY_WORLD_NAME = "world"
	const val PVP_WORLD_NAME = "uhc_pvp"
	const val GAME_WORLD_NAME = "uhc_game"
	const val NETHER_WORLD_NAME = "world_nether"
	const val END_WORLD_NAME = "world_the_end"

	fun init() {
		if (Bukkit.getWorld(PVP_WORLD_NAME) == null) internalRefreshWorld(PVP_WORLD_NAME, World.Environment.NORMAL, true)

		Bukkit.unloadWorld(END_WORLD_NAME, false)

		Bukkit.unloadWorld(GAME_WORLD_NAME, true)
		Bukkit.unloadWorld(NETHER_WORLD_NAME, true)

		Bukkit.getWorlds().forEach { prepareWorld(it) }
	}

	fun refreshGameWorlds(centerBiome: Biome?) {
		WorldGenOption.centerBiome = centerBiome

		refreshWorld(GAME_WORLD_NAME, World.Environment.NORMAL, false)
		refreshWorld(NETHER_WORLD_NAME, World.Environment.NETHER, false)
	}

	fun recoverGameWorlds() {
		WorldGenOption.centerBiome = null
		recoverWorld(GAME_WORLD_NAME)
		recoverWorld(NETHER_WORLD_NAME)
	}

	fun getLobbyWorld(): World {
		return Bukkit.getWorlds()[0]
	}

	fun getPVPWorld(): World {
		return Bukkit.getWorld(PVP_WORLD_NAME) ?: Bukkit.getWorlds()[0]
	}

	fun getGameWorld(): World? {
		return Bukkit.getWorld(GAME_WORLD_NAME)
	}

	fun getNetherWorld(): World? {
		return Bukkit.getWorld(NETHER_WORLD_NAME)
	}

	/* unsafe versions that should only be used during the game */

	fun getGameWorldGame(): World {
		return Bukkit.getWorld(GAME_WORLD_NAME)!!
	}

	fun getNetherWorldGame(): World {
		return Bukkit.getWorld(NETHER_WORLD_NAME)!!
	}

	fun isNonGameWorld(world: World): Boolean {
		return world.name == LOBBY_WORLD_NAME || world.name == PVP_WORLD_NAME
	}

	fun isGameWorld(world: World): Boolean {
		return world.name == GAME_WORLD_NAME || world.name == NETHER_WORLD_NAME
	}

	fun existsUnloaded(name: String): Boolean {
		val worldFolder = File(name)
		return worldFolder.exists() && worldFolder.isDirectory
	}

	fun prepareWorld(world: World) {
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true)
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
		world.difficulty = Difficulty.NORMAL

		if (isNonGameWorld(world)) {
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)

			world.time = 6000
			world.isThundering = false
			world.setStorm(false)

			if (world.name == LOBBY_WORLD_NAME) {
				world.worldBorder.center = Location(world, 0.5, 0.0, 0.5)
				world.worldBorder.size = Lobby.LOBBY_RADIUS * 2 + 1.0
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

	/**
	 * refresh the world without calling prepare world or bossbar
	 */
	private fun internalRefreshWorld(name: String, environment: World.Environment, structures: Boolean): Pair<World?, World?> {
		val oldWorld = destroyWorld(name)

		val creator = WorldCreator(name).environment(environment).generateStructures(structures)

		return Pair(oldWorld, creator.createWorld())
	}

	fun refreshWorld(name: String, environment: World.Environment, structures: Boolean): World? {
		val (oldWorld, newWorld) = internalRefreshWorld(name, environment, structures)

		if (newWorld != null) {
			prepareWorld(newWorld)

			if (name == GAME_WORLD_NAME) ChunkPlacerHolderType.resetAll(newWorld.seed)
		}

		return newWorld
	}

	fun recoverWorld(name: String): World? {
		val oldWorld = Bukkit.getServer().getWorld(name)
		val recovered = WorldCreator(name).createWorld()

		if (recovered != null) {
			prepareWorld(recovered)

			if (name == GAME_WORLD_NAME) ChunkPlacerHolderType.resetAll(recovered.seed)
		}

		return recovered
	}
}
