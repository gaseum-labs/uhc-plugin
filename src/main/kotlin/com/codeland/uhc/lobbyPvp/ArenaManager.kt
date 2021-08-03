package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object ArenaManager {
	const val ARENA_STRIDE = 96
	const val BORDER = 64
	const val GUTTER = 8
	const val START_BUFFER = 5
	const val TEAM_BUFFER = 3

	val ongoing = ArrayList<Arena>()
	private val typedOngoing = HashMap<ArenaType, ArrayList<Arena>>()
	fun <T : Arena> typeList(type: ArenaType): ArrayList<T> = typedOngoing.getOrPut(type) { ArrayList() } as ArrayList<T>

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
		typeList<Arena>(arena.type).add(arena)

		if (arena.type === ArenaType.PARKOUR) {
			PlayerData.playerDataList.forEach { (_, playerData) ->
				if (playerData.parkourIndex.get() == -1) playerData.parkourIndex.set(0)
			}
		}
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

			fun contain(value: Int, min: Int, max: Int): Int? {
				return if (value < min) min else if (value > max) max else null
			}

			fun teleportIn(player: Player, x: Int?, z: Int?) {
				if (x != null || z != null) {
					player.teleport(player.location.set(
						x?.toDouble() ?: player.location.x,
						player.location.y,
						z?.toDouble() ?: player.location.z
					))
				}
			}

			/* contain spectators */
			Bukkit.getOnlinePlayers()
				.filter {
					it.gameMode == GameMode.SPECTATOR &&
					it.world.name == WorldManager.PVP_WORLD_NAME
				}.forEach { player ->
					teleportIn(player,
						contain(
							player.location.blockX,
							spiral.minX() * ARENA_STRIDE,
							(spiral.maxX() + 1) * ARENA_STRIDE
						),
						contain(
							player.location.blockZ,
							spiral.minZ() * ARENA_STRIDE,
							(spiral.maxZ() + 1) * ARENA_STRIDE
						)
					)
				}

			/* contain creatives */
			Bukkit.getOnlinePlayers()
				.filter {
					it.gameMode == GameMode.CREATIVE &&
					it.world.name == WorldManager.PVP_WORLD_NAME
				}.forEach { player ->
					val arena = playersArena(player.uniqueId) ?: return@forEach

					teleportIn(player,
						contain(
							player.location.blockX,
							(arena.x * ARENA_STRIDE) + ((ARENA_STRIDE - BORDER) / 2),
							(arena.x * ARENA_STRIDE) + ARENA_STRIDE - ((ARENA_STRIDE - BORDER) / 2)
						),
						contain(
							player.location.blockZ,
							(arena.z * ARENA_STRIDE) + ((ARENA_STRIDE - BORDER) / 2),
							(arena.z * ARENA_STRIDE) + ARENA_STRIDE - ((ARENA_STRIDE - BORDER) / 2)
						)
					)
				}

			ongoing.removeIf { game ->
				val removeResult = game.perSecond()

				if (removeResult) destroyArena(game)

				removeResult
			}
		}
	}

	fun destroyArenas() {
		ongoing.removeIf { game ->
			destroyArena(game)
			true
		}
	}

	fun destroyArena(arena: Arena) {
		arena.online().forEach { removePlayer(it.uniqueId, it) }

		val typeList = typeList<Arena>(arena.type)
		typeList.removeIf { it === arena }

		if (arena.type === ArenaType.PARKOUR) {
			typeList as ArrayList<ParkourArena>
			PlayerData.playerDataList.forEach { (_, playerData) ->
				if (playerData.parkourIndex.get() >= typeList.size) {
					playerData.parkourIndex.set(typeList.lastIndex)
				}
			}
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
