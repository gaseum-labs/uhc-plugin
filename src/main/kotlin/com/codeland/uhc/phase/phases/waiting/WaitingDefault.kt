package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.phase.Phase
import org.bukkit.*
import org.bukkit.entity.Player

class WaitingDefault : Phase() {
	override fun customStart() {
		val lobbyWorld = Bukkit.getWorld(WorldManager.LOBBY_WORLD_NAME) ?: return
		val pvpWorld = Bukkit.getWorld(WorldManager.PVP_WORLD_NAME) ?: return

		/* set gamerules for all worlds */
		Bukkit.getWorlds().forEach { world -> primeWorldRules(world) }

		/* gamerules specifically for lobby worlds */
		primeLobbyWorld(lobbyWorld)
		primeLobbyWorld(pvpWorld)
		PvpData.prepareArena(pvpWorld, uhc.lobbyRadius, uhc)

		Bukkit.getServer().onlinePlayers.forEach { player ->
			player.inventory.clear()
			onPlayerJoin(player)
		}
	}

	fun primeLobbyWorld(world: World) {
		world.worldBorder.center = Location(world, 0.5, 0.0, 0.5)
		world.worldBorder.size = uhc.lobbyRadius * 2 + 1.0

		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
		world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false)

		world.time = 6000
		world.isThundering = false
		world.setStorm(false)
	}

	fun primeWorldRules(world: World) {
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true)
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
		world.difficulty = Difficulty.NORMAL
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return 1.0
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return barStatic()
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {}

	override fun endPhrase() = "Game starts in"

	fun onPlayerJoin(player: Player) {
		AbstractLobby.onSpawnLobby(player)
	}
}
