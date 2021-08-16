package com.codeland.uhc.world

import com.codeland.uhc.core.*
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolderType
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.Biomes
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import java.io.File
import kotlin.math.ceil

object WorldManager {
	const val LOBBY_WORLD_NAME = "world"
	const val PVP_WORLD_NAME = "uhc_pvp"
	const val GAME_WORLD_NAME = "uhc_game"
	const val NETHER_WORLD_NAME = "uhc_nether"

	const val BAD_NETHER_WORLD_NAME = "world_nether"
	const val END_WORLD_NAME = "world_the_end"

	var pregenTaskID = -1

	fun init() {
		if (existsUnloaded(PVP_WORLD_NAME)) {
			recoverWorld(PVP_WORLD_NAME, World.Environment.NORMAL, true)
		} else {
			refreshWorld(PVP_WORLD_NAME, World.Environment.NORMAL, true)
		}

		Bukkit.unloadWorld(END_WORLD_NAME, false)
		Bukkit.unloadWorld(BAD_NETHER_WORLD_NAME, false)

		Bukkit.unloadWorld(GAME_WORLD_NAME, true)
		Bukkit.unloadWorld(NETHER_WORLD_NAME, true)

		prepareWorld(getLobbyWorld())
	}

	fun refreshGameWorlds() {
		refreshWorld(GAME_WORLD_NAME, World.Environment.NORMAL, true)
		refreshWorld(NETHER_WORLD_NAME, World.Environment.NETHER, false)
	}

	fun recoverGameWorlds(): Boolean {
		val existed0 = recoverWorld(GAME_WORLD_NAME, World.Environment.NORMAL, true)
		val existed1 = recoverWorld(NETHER_WORLD_NAME, World.Environment.NETHER, false)

		return existed0 || existed1
	}

	fun destroyGameWorlds() {
		destroyWorld(GAME_WORLD_NAME)
		destroyWorld(NETHER_WORLD_NAME)
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
		world.difficulty = Difficulty.NORMAL
		world.setSpawnFlags(false, false)

		if (world.name == GAME_WORLD_NAME) {
			ChunkPlacerHolderType.resetAll(world.seed)

		} else if (isNonGameWorld(world)) {
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

	fun refreshWorld(name: String, environment: World.Environment, structures: Boolean) {
		destroyWorld(name)

		val creator = WorldCreator(name).environment(environment).generateStructures(structures)

		val world = creator.createWorld()

		if (world != null) prepareWorld(world)
	}

	fun recoverWorld(name: String, environment: World.Environment, structures: Boolean): Boolean {
		return if (existsUnloaded(name)) {
			val world = WorldCreator(name).createWorld()

			if (world != null) prepareWorld(world)

			true

		} else {
			refreshWorld(name, environment, structures)

			false
		}
	}
}
