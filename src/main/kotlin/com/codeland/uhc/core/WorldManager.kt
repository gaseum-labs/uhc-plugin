package com.codeland.uhc.core

import com.codeland.uhc.phase.DimensionBar
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolderType
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File

object WorldManager {
	const val LOBBY_WORLD_NAME = "world"
	const val PVP_WORLD_NAME = "uhc_pvp"
	const val GAME_WORLD_NAME = "uhc_game"
	const val NETHER_WORLD_NAME = "world_nether"

	fun initWorlds() {
		if (Bukkit.getWorld(GAME_WORLD_NAME) == null) createGameWorld()
		if (Bukkit.getWorld(PVP_WORLD_NAME) == null) createPVPWorld()

		Bukkit.unloadWorld("world_the_end", false)

		ChunkPlacerHolderType.resetAll(getGameWorld().seed)

		DimensionBar.createBossBars(Bukkit.getWorlds())
	}

	fun createPVPWorld(): World? {
		val creator = WorldCreator(PVP_WORLD_NAME)
		creator.environment(World.Environment.NORMAL)
		creator.generateStructures(true)
		return creator.createWorld()
	}

	fun createGameWorld(): World? {
		val creator = WorldCreator(GAME_WORLD_NAME)
		creator.environment(World.Environment.NORMAL)
		creator.generateStructures(false)
		return creator.createWorld()
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

	fun refreshWorld(name: String, environment: World.Environment, structures: Boolean): World? {
		Bukkit.getServer().unloadWorld(name, false)

		val file = File(name)
		if (file.exists() && file.isDirectory) file.deleteRecursively()

		val creator = WorldCreator(name)
		creator.environment(environment)
		creator.generateStructures(structures)
		return creator.createWorld()
	}
}
