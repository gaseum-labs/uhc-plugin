package com.codeland.uhc.core

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.generator.ChunkGenerator
import java.io.File

object WorldManager {
	const val LOBBY_WORLD_NAME = "uhc_lobby"
	const val PVP_WORLD_NAME = "uhc_pvp"

	fun initWorlds() {
		if (Bukkit.getWorld(LOBBY_WORLD_NAME) == null) createLobbyWorld()
		if (Bukkit.getWorld(PVP_WORLD_NAME) == null) createPVPWorld()
	}

	fun createPVPWorld(): World? {
		val creator = WorldCreator(PVP_WORLD_NAME)
		creator.environment(World.Environment.NORMAL)
		creator.generateStructures(true)
		return creator.createWorld()
	}

	fun destroyPVPWorld() {
		Bukkit.getServer().unloadWorld(PVP_WORLD_NAME, false)

		val file = File(PVP_WORLD_NAME)
		if (file.exists() && file.isDirectory) {
			file.deleteRecursively()
		}
	}

	fun createLobbyWorld(): World? {
		val creator = WorldCreator(LOBBY_WORLD_NAME)
		creator.environment(World.Environment.NORMAL)
		creator.generateStructures(true)
		return creator.createWorld()
	}

	fun getLobbyWorld(): World {
		return Bukkit.getWorld(LOBBY_WORLD_NAME) ?: Bukkit.getWorlds()[0]
	}

	fun getPVPWorld(): World {
		return Bukkit.getWorld(PVP_WORLD_NAME) ?: Bukkit.getWorlds()[0]
	}

	fun isNonGameWorld(world: World): Boolean {
		return world.name == LOBBY_WORLD_NAME || world.name == PVP_WORLD_NAME
	}
}
