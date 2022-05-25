package org.gaseumlabs.uhc.lobbyPvp

import org.gaseumlabs.uhc.core.Lobby
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.lobbyPvp.arena.ParkourArena
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.WorldStorage
import org.gaseumlabs.uhc.world.WorldManager
import org.bukkit.*
import org.bukkit.block.Structure
import org.bukkit.block.structure.UsageMode
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import java.util.*

object ArenaManager {
	const val ARENA_STRIDE = 96
	const val BORDER = 64
	const val EDGE = (ARENA_STRIDE - BORDER) / 2
	const val GUTTER = 8
	const val START_BUFFER = 5
	const val TEAM_BUFFER = 3

	val ongoing = ArrayList<Arena>()
	private val typedOngoing = HashMap<ArenaType, ArrayList<Arena>>()
	fun <T : Arena> typeList(type: ArenaType): ArrayList<T> =
		typedOngoing.getOrPut(type) { ArrayList() } as ArrayList<T>

	val spiral = Spiral()

	fun addArena(arena: Arena, coords: Pair<Int, Int>? = null) {
		if (coords == null) {
			arena.x = spiral.getX()
			arena.z = spiral.getZ()
			spiral.next()
		} else {
			arena.x = coords.first
			arena.z = coords.second
		}

		/* set last played against for players */
		/* can only set last played against for one of the players on the other team */
		for (i in 0..arena.teams.lastIndex - 1) {
			val otherTeamIndex = arena.teams.indices.firstOrNull { it != i } ?: continue

			arena.teams[i].zip(arena.teams[otherTeamIndex]).forEach { (playerA, playerB) ->
				PlayerData.getPlayerData(playerA).lastPlayed = playerB
				PlayerData.getPlayerData(playerB).lastPlayed = playerA
			}
		}

		if (coords == null) arena.prepareArena(WorldManager.pvpWorld)

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
		/* prevent shooting cross arena */
		if (currentTick % 4 == 0) {
			WorldManager.pvpWorld.entities.forEach { entity ->
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
					it.gameMode == GameMode.SPECTATOR && it.world === WorldManager.pvpWorld
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
					it.gameMode == GameMode.CREATIVE && it.world === WorldManager.pvpWorld
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

	fun destroyArenas(world: World) {
		ongoing.removeIf { game ->
			destroyArena(game)
			true
		}

		WorldStorage.destroy(world, 1, 1)
		WorldStorage.destroy(world, 2, 2)

		spiral.reset()
	}

	fun destroyArena(arena: Arena) {
		arena.online().forEach { player ->
			removePlayer(player.uniqueId)

			val playerData = PlayerData.getPlayerData(player.uniqueId)
			Lobby.onSpawnLobby(player)
			player.inventory.setContents(playerData.lobbyInventory)
		}

		val typeList = typeList<Arena>(arena.type)
		typeList.removeIf { it === arena }

		WorldStorage.destroy(WorldManager.pvpWorld, arena.x * ARENA_STRIDE, arena.z * ARENA_STRIDE)

		if (arena.type === ArenaType.PARKOUR) {
			typeList as ArrayList<ParkourArena>
			PlayerData.playerDataList.forEach { (_, playerData) ->
				if (playerData.parkourIndex.get() >= typeList.size) {
					playerData.parkourIndex.set(typeList.lastIndex)
				}
			}
		}
	}

	fun removePlayer(uuid: UUID) {
		val game = playersArena(uuid)
		game?.teams?.any { team -> team.removeIf { it == uuid } }
	}

	/* saving arena data */

	fun encodeArenaLocations(arenas: List<Arena>): String {
		return arenas.joinToString("|") { arena -> "${arena.x},${arena.z}" }
	}

	fun decodeArenaLocations(data: String): List<Pair<Int, Int>> {
		return data.split('|').map { str ->
			val parts = str.split(',')
			(parts[0].toIntOrNull() ?: return emptyList()) to (parts[1].toIntOrNull() ?: return emptyList())
		}
	}

	fun saveWorldInfo(world: World) {
		WorldStorage.save(world, 1, 1, spiral.toMetadata())
		WorldStorage.save(world, 2, 2,
			encodeArenaLocations(ongoing.filter { arena -> arena.save(world) }))
	}

	fun loadWorldInfo(world: World) {
		val spiralData = WorldStorage.load(world, 1, 1) ?: return
		spiral.fromMetadata(spiralData)

		val arenaLocationsData = WorldStorage.load(world, 2, 2) ?: return
		decodeArenaLocations(arenaLocationsData).forEach { (x, z) ->
			val loadedArena = Arena.load(world, x, z)
			if (loadedArena != null) addArena(loadedArena, Pair(x, z))
		}
	}
}
