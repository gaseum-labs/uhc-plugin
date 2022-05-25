package org.gaseumlabs.uhc.lobbyPvp

import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.component.ComponentAction.uhcHotbar
import org.gaseumlabs.uhc.component.ComponentAction.uhcTitle
import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.team.NameManager
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.WorldStorage
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
import java.util.*
import kotlin.collections.ArrayList

abstract class Arena(val type: ArenaType, val teams: ArrayList<ArrayList<UUID>>) {
	var x = 0
	var z = 0
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

		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* save before pvp state */
		playerData.lobbyInventory = player.inventory.contents!!.clone()

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

	/**
	 * @return if this arena is saveable
	 */
	fun save(world: World): Boolean {
		val saveData = customSave()

		return if (saveData != null) {
			WorldStorage.save(
				world,
				x * ARENA_STRIDE,
				z * ARENA_STRIDE,
				"${type.name}|${saveData}"
			)

			true
		} else {
			false
		}
	}

	abstract fun customPerSecond(onlinePlayers: List<Player>): Boolean

	abstract fun startingPositions(teams: ArrayList<ArrayList<UUID>>): List<List<Position>>

	abstract fun customStartPlayer(player: Player, playerData: PlayerData)

	abstract fun prepareArena(world: World)

	abstract fun arenaStart(onlinePlayers: List<Player>)

	abstract fun startText(): String

	abstract fun shutdownOnLeave(): Boolean

	abstract fun customSave(): String?

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

	fun getCenter(): Pair<Int, Int> = Companion.getCenter(x, z)

	fun inBorder(testX: Int, testZ: Int): Boolean {
		return testX in x * ARENA_STRIDE + EDGE until x * ARENA_STRIDE + EDGE + BORDER &&
		testZ in z * ARENA_STRIDE + EDGE until z * ARENA_STRIDE + EDGE + BORDER
	}

	companion object {
		fun getCenter(x: Int, z: Int): Pair<Int, Int> {
			return Pair(
				x * ARENA_STRIDE + (ARENA_STRIDE / 2),
				z * ARENA_STRIDE + (ARENA_STRIDE / 2)
			)
		}

		fun load(world: World, x: Int, z: Int): Arena? {
			val data = WorldStorage.load(
				world,
				x * ARENA_STRIDE,
				z * ARENA_STRIDE
			) ?: return null

			val parts = data.split('|')
			if (parts.size != 2) return null

			val typeName = parts[0]
			val arenaType = ArenaType.values().find { it.name == typeName } ?: return null

			val arena = arenaType.load(parts[1], world) ?: return null
			arena.x = x
			arena.z = z

			return arena
		}
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