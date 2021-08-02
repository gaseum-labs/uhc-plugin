package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

object ArenaManager {
	const val ARENA_STRIDE = 96
	const val BORDER = 64
	const val GUTTER = 8
	const val START_BUFFER = 5
	const val TEAM_BUFFER = 3

	val ongoing = ArrayList<Arena>()

	val spiral = Spiral()

	fun addArena(arena: Arena) {
		arena.x = spiral.getX()
		arena.z = spiral.getZ()

		spiral.next()

		/* set last played against for players */
		/* can only set last played against for one of the players on the other team */
		for (i in 0..arena.teams.lastIndex - 1) {
			val otherTeamIndex = arena.teams.indices.firstOrNull { it != i } ?: continue

			arena.teams[i].zip(arena.teams[otherTeamIndex]).forEach { (playerA, playerB) ->
				PlayerData.getPlayerData(playerA).lastPlayed = playerB
				PlayerData.getPlayerData(playerB).lastPlayed = playerA
			}
		}

		arena.prepareArena(WorldManager.getPVPWorld())

		ongoing.add(arena)
	}

	fun playersArena(uuid: UUID): Arena? {
		return ongoing.find { game -> game.teams.any { team -> team.any { it == uuid } } }
	}

	fun playersTeam(game: Arena, uuid: UUID): ArrayList<UUID>? {
		return game.teams.find { it.contains(uuid) }
	}

	fun onEdge(x: Int, z: Int): Boolean {
		val x = Util.mod(x, ARENA_STRIDE)
		val z = Util.mod(z, ARENA_STRIDE)

		return x < GUTTER || x > ARENA_STRIDE - GUTTER || z < GUTTER || z > ARENA_STRIDE - GUTTER
	}

	fun perTick(currentTick: Int) {
		if (currentTick % 4 == 0) {
			WorldManager.getPVPWorld().entities.forEach { entity ->
				if (
					entity is AbstractArrow &&
					onEdge(entity.location.blockX, entity.location.blockZ)
				) entity.remove()
			}
		}

		if (currentTick % 20 == 0) {
			PvpQueue.perSecond()

			/* contain spectators */
			Bukkit.getOnlinePlayers()
				.filter {
					it.gameMode == GameMode.SPECTATOR &&
					it.world.name == WorldManager.PVP_WORLD_NAME
				}.forEach { player ->
					fun contain(value: Int, min: Int, max: Int): Int? {
						return if (value < min) min else if (value > max) max else null
					}

					val teleportX = contain(
						player.location.blockX,
						spiral.minX() * ARENA_STRIDE,
						(spiral.maxX() + 1) * ARENA_STRIDE
					)
					val teleportZ = contain(
						player.location.blockZ,
						spiral.minZ() * ARENA_STRIDE,
						(spiral.maxZ() + 1) * ARENA_STRIDE
					)

					if (teleportX != null || teleportZ != null) {
						player.teleport(player.location.set(
							teleportX?.toDouble() ?: player.location.x,
							player.location.y,
							teleportZ?.toDouble() ?: player.location.z
						))
					}
				}

			ongoing.removeIf { game ->
				val removeResult = game.perSecond()

				if (removeResult) {
					game.online().forEach { removePlayer(it.uniqueId, it) }
				}

				removeResult
			}
		}
	}

	fun destroyArenas() {
		/* delete games and remove active players from them */
		ongoing.removeIf { game ->
			game.teams.forEach {
				team -> team.mapNotNull { Bukkit.getPlayer(it) }.forEach { removePlayer(it.uniqueId, it) }
			}
			true
		}
	}

	fun removePlayer(uuid: UUID, player: Player? = null) {
		val game = playersArena(uuid)
		game?.teams?.any { team -> team.removeIf { it == uuid } }

		/* if the player is online */
		val player = player ?: (Bukkit.getPlayer(uuid) ?: return)

		val playerData = PlayerData.getPlayerData(player.uniqueId)

		Lobby.onSpawnLobby(player)

		player.inventory.contents = playerData.lobbyInventory
	}
}
