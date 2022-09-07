package org.gaseumlabs.uhc.lobbyPvp

import org.gaseumlabs.uhc.core.Lobby
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.lobbyPvp.arena.ParkourArena
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.WorldManager
import org.bukkit.*
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena
import org.gaseumlabs.uhc.util.Coords
import org.gaseumlabs.uhc.util.WorldStorage
import org.gaseumlabs.uhc.util.extensions.ArrayListExtensions.removeRef
import java.util.*
import kotlin.collections.ArrayList

object ArenaManager {
	const val ARENA_STRIDE = 96
	const val BORDER = 64
	const val EDGE = (ARENA_STRIDE - BORDER) / 2
	const val GUTTER = 8
	const val START_BUFFER = 5
	const val TEAM_BUFFER = 3

	var spiral = Spiral.defaultSpiral()
	val ongoing = ArrayList<Arena>()

	inline fun <reified A: Arena>ongoingOf() = ongoing.filterIsInstance<A>()

	fun nextCoords(): Coords {
		val x = spiral.getX()
		val z = spiral.getZ()
		spiral.next()
		return Coords(x, z)
	}

	fun addNewArena(arena: Arena) {
		/* set last played against for players */
		/* can only set last played against for one of the players on the other team */
		for (i in 0 until arena.teams.lastIndex) {
			val otherTeamIndex = arena.teams.indices.firstOrNull { it != i } ?: continue

			arena.teams[i].zip(arena.teams[otherTeamIndex]).forEach { (playerA, playerB) ->
				PlayerData.get(playerA).lastPlayed = playerB
				PlayerData.get(playerB).lastPlayed = playerA
			}
		}

		arena.prepareArena(WorldManager.pvpWorld)

		ongoing.add(arena)
	}

	fun addExistingArena(arena: Arena) {
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

	private fun cleanKickPlayersOut(arena: Arena) = arena.online().forEach { player ->
		val playerData = PlayerData.get(player.uniqueId)
		Lobby.onSpawnLobby(player)
		player.inventory.contents = playerData.lobbyInventory
	}

	private fun updateParkourIndices() = PlayerData.playerDataList.forEach {
		(_, playerData) -> playerData.parkourIndex = 0
	}

	fun destroyAllArenas() {
		spiral = Spiral.defaultSpiral()

		ongoing.forEach(::cleanKickPlayersOut)
		ongoing.clear()

		updateParkourIndices()
		ParkourArena.premiereArena = null
	}

	fun destroyArena(arena: Arena) {
		cleanKickPlayersOut(arena)

		ongoing.removeRef(arena)

		if (arena is ParkourArena) {
			updateParkourIndices()
			if (arena === ParkourArena.premiereArena)
				ParkourArena.premiereArena = null
		}
	}

	fun removePlayer(uuid: UUID) {
		val game = playersArena(uuid)
		game?.teams?.any { team -> team.removeIf { it == uuid } }
	}

	fun saveWorldInfo(world: World) {
		WorldStorage.setData(world, Spiral.key, Spiral.spiralData, spiral)

		val arenaMarkers = ongoing.filterIsInstance<ParkourArena>().map { arena ->
			ArenaMarker(
				Coords(arena.x, arena.z),
				arena.startPosition,
				arena.owner,
				arena === ParkourArena.premiereArena
			)
		} as ArrayList<ArenaMarker>

		WorldStorage.setData(world, ArenaMarker.key, ArenaMarker.dataType, arenaMarkers)

		WorldStorage.setData(
			world,
			PlatformStorage.key,
			PlatformStorage.dataType,
			GapSlapArena.submittedPlatforms.map { (_, platform) -> platform.storage } as ArrayList<PlatformStorage>
		)
	}

	fun loadWorldInfo(world: World) {
		val dataSpiral = WorldStorage.getData(world, Spiral.key, Spiral.spiralData)
		if (dataSpiral != null) spiral = dataSpiral

		val arenaMarkers = WorldStorage.getData(world, ArenaMarker.key, ArenaMarker.dataType)
		arenaMarkers?.forEach { marker ->
			val arena = ParkourArena(arrayListOf(), marker.coords, marker.owner, marker.start)
			addExistingArena(arena)
			if (marker.premiere) ParkourArena.premiereArena = arena
		}

		val platformStorage = WorldStorage.getData(world, PlatformStorage.key, PlatformStorage.dataType)
		platformStorage?.forEach { platform ->
			try {
				GapSlapArena.submittedPlatforms[platform.owner] = Platform.fromStorage(world, platform)
			} catch (ex: Exception) {
				Util.log("WARNING: Bad gap slap platform loaded: ${ex.message}")
			}
		}
	}
}
