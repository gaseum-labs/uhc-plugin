package com.codeland.uhc.core

import com.codeland.uhc.phase.WorldBar
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolderType
import org.bukkit.*
import java.io.File

object WorldManager {
	const val LOBBY_WORLD_NAME = "world"
	const val PVP_WORLD_NAME = "uhc_pvp"
	const val GAME_WORLD_NAME = "uhc_game"
	const val NETHER_WORLD_NAME = "world_nether"
	const val END_WORLD_NAME = "world_the_end"

	fun initWorlds(reload: Boolean) {
		if (Bukkit.getWorld(GAME_WORLD_NAME) == null || reload) internalRefreshWorld(GAME_WORLD_NAME, World.Environment.NORMAL, false)
		if (Bukkit.getWorld(PVP_WORLD_NAME) == null || reload) internalRefreshWorld(PVP_WORLD_NAME, World.Environment.NORMAL, true)
		if (Bukkit.getWorld(NETHER_WORLD_NAME) == null || reload) internalRefreshWorld(NETHER_WORLD_NAME, World.Environment.NETHER, false)

		Bukkit.unloadWorld(END_WORLD_NAME, false)

		WorldBar.initWorldBars(Bukkit.getWorlds())
		ChunkPlacerHolderType.resetAll(getGameWorld().seed)
		Bukkit.getWorlds().forEach { prepareWorld(it) }
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
				world.worldBorder.size = AbstractLobby.LOBBY_RADIUS * 2 + 1.0
			}
		}
	}

	fun getLobbyWorld(): World {
		return Bukkit.getWorlds()[0]
	}

	fun getPVPWorld(): World {
		return Bukkit.getWorld(PVP_WORLD_NAME) ?: Bukkit.getWorlds()[0]
	}

	fun getGameWorld(): World {
		return Bukkit.getWorld(GAME_WORLD_NAME) ?: Bukkit.getWorlds()[0]
	}

	fun getNetherWorld(): World {
		return Bukkit.getWorld(NETHER_WORLD_NAME) ?: Bukkit.getWorlds()[0]
	}

	fun isNonGameWorld(world: World): Boolean {
		return world.name == LOBBY_WORLD_NAME || world.name == PVP_WORLD_NAME
	}

	fun isGameWorld(world: World): Boolean {
		return world.name == GAME_WORLD_NAME || world.name == NETHER_WORLD_NAME
	}

	private fun internalRefreshWorld(name: String, environment: World.Environment, structures: Boolean): Pair<World?, World?> {
		val oldWorld = Bukkit.getServer().getWorld(name)

		if (oldWorld != null) {
			Bukkit.getServer().unloadWorld(oldWorld, false)

			val file = File(name)
			if (file.exists() && file.isDirectory) file.deleteRecursively()
		}

		val creator = WorldCreator(name).environment(environment).generateStructures(structures)

		return Pair(oldWorld, creator.createWorld())
	}

	fun refreshWorld(name: String, environment: World.Environment, structures: Boolean): World? {
		val (oldWorld, newWorld) = internalRefreshWorld(name, environment, structures)

		if (newWorld != null) {
			WorldBar.resetWorldBar(oldWorld, newWorld)
			prepareWorld(newWorld)

			if (name == GAME_WORLD_NAME) ChunkPlacerHolderType.resetAll(newWorld.seed)
		}

		return newWorld
	}
}
