package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.item.CommandItemType
import com.codeland.uhc.util.Util
import com.codeland.uhc.gui.item.ParkourCheckpoint
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.quirk.quirks.Pests
import com.codeland.uhc.team.TeamData
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.entity.Player

class WaitingDefault : Phase() {
	companion object {
		val oceans = arrayOf(
			Biome.OCEAN,
			Biome.DEEP_OCEAN,
			Biome.COLD_OCEAN,
			Biome.DEEP_COLD_OCEAN,
			Biome.FROZEN_OCEAN,
			Biome.DEEP_FROZEN_OCEAN,
			Biome.LUKEWARM_OCEAN,
			Biome.DEEP_LUKEWARM_OCEAN,
			Biome.WARM_OCEAN
		)

		fun validLobbySpot(world: World, x: Int, z: Int, radius: Int): Boolean {
			val halfRadius = radius / 2

			return Util.topLiquidSolidY(world, x, z).first == -1 &&
				Util.topLiquidSolidY(world, x + halfRadius, z + halfRadius).first == -1 &&
				Util.topLiquidSolidY(world, x - halfRadius, z + halfRadius).first == -1 &&
				Util.topLiquidSolidY(world, x + halfRadius, z - halfRadius).first == -1 &&
				Util.topLiquidSolidY(world, x - halfRadius, z - halfRadius).first == -1
		}

		fun teleportPlayerCenter(uhc: UHC, player: Player) {
			player.teleport(Location(Bukkit.getWorlds()[0], uhc.lobbyX + 0.5, Util.topBlockYTop(Bukkit.getWorlds()[0], 254, uhc.lobbyX, uhc.lobbyZ) + 1.0, uhc.lobbyZ + 0.5))
		}
	}

	override fun customStart() {
		val world = Bukkit.getWorlds()[0]
		
		fun findSpot(signX: Int, signZ: Int): Pair<Int, Int> {
			var x: Int
			var z: Int
			var tries = 0

			do {
				x = Util.randRange(10000, 100000) * signX
				z = Util.randRange(10000, 100000) * signZ
				++tries
			} while (!validLobbySpot(world, x, z, uhc.lobbyRadius) && tries < 100)

			return Pair(x, z)
		}

		if (uhc.lobbyX == -1) {
			val (x, z) = findSpot(1, 1)
			uhc.lobbyX = x
			uhc.lobbyZ = z

			LobbyPvp.createArena(world, x, z, uhc.lobbyRadius)
		}

		if (uhc.lobbyPvpX == -1) {
			val (x, z) = findSpot(-1, -1)
			uhc.lobbyPvpX = x
			uhc.lobbyPvpZ = z

			LobbyPvp.createArena(world, x, z, uhc.lobbyRadius)
		}

		world.setSpawnLocation(uhc.lobbyX, Util.topBlockYTop(world, 254, uhc.lobbyX, uhc.lobbyZ) + 1, uhc.lobbyZ)

		world.worldBorder.reset()

		world.isThundering = false
		world.setStorm(false)
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false)
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
		world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
		world.time = 6000
		world.difficulty = Difficulty.NORMAL

		TeamData.removeAllTeams { player ->
			uhc.setParticipating(player, false)
		}

		Bukkit.getServer().onlinePlayers.forEach { player ->
			player.inventory.clear()
			onPlayerJoin(player)
		}
	}

	override fun customEnd() {
		Bukkit.getWorlds().forEach { world ->
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
			world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3)
		}
		LobbyPvp.pvpMap.clear()
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return 1.0
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return barStatic()
	}

	override fun perTick(currentTick: Int) {
		if (currentTick % 3 == 0) {
			Bukkit.getOnlinePlayers().forEach { player ->
				ParkourCheckpoint.updateCheckpoint(player)
			}
		}
	}

	override fun perSecond(remainingSeconds: Int) {}

	override fun endPhrase() = "Game starts in"

	fun onPlayerJoin(player: Player) {
		player.exp = 0.0F
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.foodLevel = 20
		teleportPlayerCenter(uhc, player)
		player.gameMode = GameMode.CREATIVE

		Pests.makeNotPest(player)

		/* get them on the health scoreboard */
		player.damage(0.05)

		val inventory = player.inventory

		CommandItemType.giveItem(CommandItemType.GUI_OPENER, inventory)
		CommandItemType.giveItem(CommandItemType.JOIN_PVP, inventory)
		CommandItemType.giveItem(CommandItemType.PARKOUR_CHECKPOINT, inventory)
	}
}
