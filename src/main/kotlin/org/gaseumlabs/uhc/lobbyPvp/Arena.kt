package org.gaseumlabs.uhc.lobbyPvp

import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.component.ComponentAction.uhcHotbar
import org.gaseumlabs.uhc.component.ComponentAction.uhcTitle
import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.team.NameManager
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.WorldManager
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket
import net.minecraft.world.level.border.WorldBorder
import org.bukkit.*
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager.ARENA_STRIDE
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager.BORDER
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager.EDGE
import org.gaseumlabs.uhc.util.Coords
import java.util.*
import kotlin.collections.ArrayList

abstract class Arena(val teams: ArrayList<ArrayList<UUID>>, coords: Coords) {
	val x = coords.x
	val z = coords.z
	var startTime = -4

	data class Position(val x: Int, val z: Int, val rotation: Float, val y: Int?)

	fun perSecond(): Boolean {
		++startTime
		val onlinePlayers = online()

		return if (startTime < 0) {
			onlinePlayers.forEach { player ->
				player.uhcTitle(
					UHCComponent.text((-startTime).toString(), UHCColor.U_RED),
					UHCComponent.text(startText(), UHCColor.U_RED),
					0, 21, 0
				)
				player.uhcHotbar(UHCComponent.text())
			}

			false

		} else if (startTime == 0) {
			/* everyone must be there to start */
			if (all().any { Bukkit.getPlayer(it) == null }) {
				onlinePlayers.forEach { player -> Commands.errorMessage(player, "Game cancelled! A player left") }

				true

			} else {
				val world = WorldManager.pvpWorld

				val positions = startingPositions(teams)

				val teamLocations = positions.map {
					it.map { (x, z, rotation, fixedY) ->
						val y = if (fixedY != null) {
							fixedY
						} else {
							val (liquidY, solidY) = Util.topLiquidSolidY(world, x, z)
							if (liquidY != -1) world.getBlockAt(x, liquidY, z).type = Material.STONE

							(if (liquidY == -1) solidY else liquidY) + 1
						}

						Location(
							world,
							x + 0.5,
							y.toDouble(),
							z + 0.5,
							rotation,
							0.0f
						)
					}
				}

				teams.zip(teamLocations).forEach { (team, teamLocation) ->
					team.zip(teamLocation).forEach { (uuid, location) ->
						val player = Bukkit.getPlayer(uuid)
						if (player != null) startPlayer(player, location)
					}
				}

				arenaStart(onlinePlayers)

				false
			}
		} else {
			/* clear non-players */
			teams.forEach { team ->
				team.removeIf { !playerIsParticipating(it) }
			}

			(shutdownOnLeave() && onlinePlayers.isEmpty()) || customPerSecond(onlinePlayers)
		}
	}

	fun startPlayer(player: Player, location: Location) {
		player.closeInventory()

		val playerData = PlayerData.get(player.uniqueId)

		/* save before pvp state */
		playerData.lobbyInventory = player.inventory.contents.clone()

		Lobby.resetPlayerStats(player)

		player.teleport(location)
		customStartPlayer(player, playerData)
		NameManager.updateNominalTeams(player, UHC.getTeams().playersTeam(player.uniqueId), false)

		/* fake border */
		val border = WorldBorder()
		border.world = (WorldManager.pvpWorld as CraftWorld).handle
		val (centerX, centerZ) = getCenter()
		border.setCenter(centerX.toDouble(), centerZ.toDouble())
		border.size = BORDER.toDouble()

		player as CraftPlayer
		player.handle.connection.send(ClientboundSetBorderCenterPacket(border))
		player.handle.connection.send(ClientboundSetBorderSizePacket(border))
	}

	abstract fun customPerSecond(onlinePlayers: List<Player>): Boolean

	abstract fun startingPositions(teams: ArrayList<ArrayList<UUID>>): List<List<Position>>

	abstract fun customStartPlayer(player: Player, playerData: PlayerData)

	abstract fun prepareArena(world: World)

	abstract fun arenaStart(onlinePlayers: List<Player>)

	abstract fun startText(): String

	abstract fun shutdownOnLeave(): Boolean

	/* utility */

	fun all() = teams.flatten()

	fun online() = teams.flatMap { team ->
		team.mapNotNull { Bukkit.getPlayer(it) }
	}

	fun alive() = teams.flatMap { team -> team.mapNotNull { alivePlayer(it) } }

	fun teamsAlive() = teams.map { team ->
		team.mapNotNull { alivePlayer(it) }
	}.filter { it.isNotEmpty() }

	fun alivePlayer(uuid: UUID): Player? {
		val player = Bukkit.getPlayer(uuid) ?: return null
		return if (playerIsAlive(player)) player else null
	}

	fun playerIsAlive(player: Player): Boolean {
		return player.location.world === WorldManager.pvpWorld &&
		player.gameMode !== GameMode.SPECTATOR
	}

	fun playerIsParticipating(uuid: UUID): Boolean {
		return Bukkit.getPlayer(uuid)?.location?.world === WorldManager.pvpWorld
	}

	fun getCenter(): Coords = Companion.getCenter(x, z)

	fun inBorder(testX: Int, testZ: Int): Boolean {
		return testX in x * ARENA_STRIDE + EDGE until x * ARENA_STRIDE + EDGE + BORDER &&
		testZ in z * ARENA_STRIDE + EDGE until z * ARENA_STRIDE + EDGE + BORDER
	}

	companion object {
		fun getCenter(x: Int, z: Int) = Coords(
			x * ARENA_STRIDE + (ARENA_STRIDE / 2),
			z * ARENA_STRIDE + (ARENA_STRIDE / 2)
		)

		fun arenaCoordsAt(blockX: Int, blockZ: Int) = Coords(
			Util.mod(blockX, ARENA_STRIDE),
			Util.mod(blockZ, ARENA_STRIDE),
		)
	}

	fun outsideBorder(onOutside: (Player, Int) -> Unit) {
		online().forEach { player ->
			val amount = playerOutsideBorderBy(player)
			if (amount > 0) onOutside(player, amount)
		}
	}

	fun playerOutsideBorderBy(player: Player): Int {
		val minX = (x * ARENA_STRIDE) + (ARENA_STRIDE - BORDER) / 2
		val maxX =
			(x * ARENA_STRIDE) + ((ARENA_STRIDE / 2) + (BORDER / 2)) - 1
		val minZ = (z * ARENA_STRIDE) + (ARENA_STRIDE - BORDER) / 2
		val maxZ =
			(z * ARENA_STRIDE) + ((ARENA_STRIDE / 2) + (BORDER / 2)) - 1

		val playerX = player.location.blockX
		val playerZ = player.location.blockZ

		val outside = when {
			playerX < minX -> minX - playerX
			playerX > maxX -> playerX - maxX
			else -> 0
		} + when {
			playerZ < minZ -> minZ - playerZ
			playerZ > maxZ -> playerZ - maxZ
			else -> 0
		}

		return outside
	}
}