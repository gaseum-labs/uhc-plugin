package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.event.Packet
import com.codeland.uhc.team.NameManager
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket
import net.minecraft.world.level.border.WorldBorder
import org.bukkit.*
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

object PvpGameManager {
	const val ARENA_STRIDE = 80
	const val BORDER = 64

	const val TYPE_1V1 = 1
	const val TYPE_2V2 = 2

	fun typeName(type: Int) = if (type == TYPE_1V1) "1v1" else "2v2"

	class PvpGame(val teams: ArrayList<ArrayList<UUID>>, val type: Int, val x: Int, val z: Int, val borderSize: Int) {
		var winners: List<UUID> = ArrayList()
		var time = -4

		var glowPeriod = 60
		var glowTimer = glowPeriod

		fun online() = teams.flatMap { team ->
			team.mapNotNull { Bukkit.getPlayer(it) }
		}

		fun alivePlayer(uuid: UUID): Player? {
			val player = Bukkit.getPlayer(uuid) ?: return null

			return if (
				player.location.world.name == WorldManager.PVP_WORLD_NAME &&
				player.gameMode != GameMode.SPECTATOR
			) {
				player
			} else {
				null
			}
		}

		fun alive() = teams.flatMap { team -> team.mapNotNull { alivePlayer(it) } }

		fun teamsAlive() = teams.map { team ->
			team.mapNotNull { alivePlayer(it) }
		}.filter { it.isNotEmpty() }

		fun endNaturally(winners: List<Player>) {
			this.winners = winners.map { it.uniqueId }
			time = -10

			val winnerString = if (winners.size == 1) {
				"${ChatColor.RED}${winners.first().name} wins!"
			} else {
				winners.joinToString(" ", "${ChatColor.RED}", " have won!") { it.name }
			}

			online().forEach { it.sendTitle(winnerString, "", 0, 160, 40) }

			updateGlowAll()
		}

		/**
		 * @return should the game be immediately deleted
		 */
		fun checkEnd(): Boolean {
			val teamsAlive = teamsAlive()

			/* one team remains, end naturally */
			return if (teamsAlive.size == 1) {
				endNaturally(teamsAlive.first())
				false

			/* if somehow all players disconnect then end it immediately */
			} else {
				teamsAlive.isEmpty()
			}
		}

		fun prepareArena() {
			val world = WorldManager.getPVPWorld()
			val border = (ARENA_STRIDE - BORDER) / 2

			for (bx in x * ARENA_STRIDE until (x + 1) * ARENA_STRIDE) {
				for (bz in z * ARENA_STRIDE until (z + 1) * ARENA_STRIDE) {
					/* bedrock floor below sea level */
					val aboveBlock = world.getBlockAt(bx, 62, bz)

					if (aboveBlock.type.isAir || aboveBlock.isLiquid) {
						world.getBlockAt(bx, 61, bz).setType(Material.SAND, false)
						world.getBlockAt(bx, 60, bz).setType(Material.BEDROCK, false)
					} else {
						world.getBlockAt(bx, 61, bz).setType(Material.BEDROCK, false)
					}

					/* super mountain clearer */
					for (by in 101..255) world.getBlockAt(bx, by, bz).setType(Material.AIR, false)
				}
			}
		}

		fun shouldGlow(): Boolean {
			return glowTimer <= 0 && !isOver()
		}

		fun updateGlowAll() {
			val online = online()

			online.forEach { player1 ->
				online.forEach { player2 ->
					(player1 as CraftPlayer).handle.b.sendPacket(Packet.metadataPacketDefaultState(player2))
				}
			}
		}

		fun isOver(): Boolean {
			return winners.isNotEmpty()
		}

		fun getCenter(): Pair<Int, Int> {
			return center(x, z)
		}

		companion object {
			fun center(x: Int, z: Int): Pair<Int, Int> {
				return Pair(
					x * ARENA_STRIDE + (ARENA_STRIDE / 2),
					z * ARENA_STRIDE + (ARENA_STRIDE / 2)
				)
			}
		}
	}

	val ongoingGames = ArrayList<PvpGame>()

	var currentDir = 0

	/* positive x, positive z, negative x, negative z */
	val axes = arrayOf(0, 0)
	val maxes = arrayOf(0, 0, 0, 0)

	fun currentAxis(dir: Int) = dir % 2
	fun increment(dir: Int) = 1 - (dir / 2) * 2

	fun addGame(teams: ArrayList<ArrayList<UUID>>, type: Int) {
		val game = PvpGame(teams, type, axes[0], axes[1], BORDER)

		/* next position in a spiral */
		axes[currentAxis(currentDir)] += increment(currentDir)
		if (abs(axes[currentAxis(currentDir)]) > maxes[currentDir]) {
			maxes[currentDir] = abs(axes[currentAxis(currentDir)])
			currentDir = (currentDir + 1) % 4
		}

		/* set last played against for players */
		/* can only set last played against for one of the players on the other team */
		for (i in 0..teams.lastIndex - 1) {
			val otherTeamIndex = teams.indices.firstOrNull { it != i } ?: continue

			teams[i].zip(teams[otherTeamIndex]).forEach { (playerA, playerB) ->
				PlayerData.getPlayerData(playerA).lastPlayed = playerB
				PlayerData.getPlayerData(playerB).lastPlayed = playerA
			}
		}

		game.prepareArena()

		ongoingGames.add(game)
	}

	fun playersGame(uuid: UUID): PvpGame? {
		return ongoingGames.find { game -> game.teams.any { it.any { it == uuid } } }
	}

	fun playersTeam(game: PvpGame, uuid: UUID): ArrayList<UUID>? {
		return game.teams.find { it.contains(uuid) }
	}

	fun onEdge(x: Int, z: Int): Boolean {
		val x = Util.mod(x, ARENA_STRIDE)
		val z = Util.mod(z, ARENA_STRIDE)
		val edge = (ARENA_STRIDE - BORDER) / 2

		return x < edge || x > ARENA_STRIDE - edge || z < edge || z > ARENA_STRIDE - edge
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
						-maxes[2] * ARENA_STRIDE,
						(maxes[0] + 1) * ARENA_STRIDE
					)
					val teleportZ = contain(
						player.location.blockZ,
						-maxes[3] * ARENA_STRIDE,
						(maxes[1] + 1) * ARENA_STRIDE
					)

					if (teleportX != null || teleportZ != null) {
						player.teleport(player.location.set(
							teleportX?.toDouble() ?: player.location.x,
							player.location.y,
							teleportZ?.toDouble() ?: player.location.z
						))
					}
				}

			ongoingGames.removeIf { game ->
				++game.time

				/* postgame length until game is destroyed */
				val removeResult = if (game.isOver()) {
					game.time >= 0

				/* before and during the game */
				} else when {
					/* countdown before match starts */
					game.time < 0 -> {
						game.online().forEach { player ->
							player.sendTitle("${ChatColor.RED}${-game.time}", "${ChatColor.RED}PVP Match Starting", 0, 21, 0)
							player.sendActionBar(Component.text(""))
						}

						false
					}
					/* start match */
					game.time == 0 -> {
						/* everyone must be there to start */
						if (game.teams.flatten().any { Bukkit.getPlayer(it) == null }) {
							game.online().forEach { player -> Commands.errorMessage(player, "Game cancelled! A player left") }

							true

						} else {
							val world = WorldManager.getPVPWorld()
							val locations = teamPositions(game).map { position ->
								val (liquidY, solidY) = Util.topLiquidSolidY(world, position.first, position.second)
								Location(world, position.first + 0.5, (if (liquidY == -1) solidY else liquidY) + 1.0, position.second + 0.5)
							}

							game.teams.zip(locations).forEach { (team, location) ->
								team.forEach { uuid ->
									val player = Bukkit.getPlayer(uuid)
									if (player != null) {
										enablePvp(player, true, location)
										player.sendTitle("${ChatColor.GOLD}FIGHT", "", 0, 20, 10)
									}
								}
							}

							false
						}
					}
					/* during match */
					else -> {
						/* damage if outside the border */
						game.online().forEach { player ->
							val minX = (game.x * ARENA_STRIDE) + (ARENA_STRIDE - BORDER) / 2
							val maxX = (game.x * ARENA_STRIDE) + ((ARENA_STRIDE / 2) + (BORDER / 2)) - 1
							val minZ = (game.z * ARENA_STRIDE) + (ARENA_STRIDE - BORDER) / 2
							val maxZ = (game.z * ARENA_STRIDE) + ((ARENA_STRIDE / 2) + (BORDER / 2)) - 1

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

							if (outside > 0) player.damage(outside / 2.0)
						}

						if (game.glowPeriod > 0) {
							--game.glowTimer

							if (game.glowTimer == 0) {
								game.updateGlowAll()

							} else if (game.glowTimer <= -2) {
								game.glowPeriod /= 2
								game.glowTimer = game.glowPeriod
								game.updateGlowAll()
							}
						}

						game.checkEnd()
					}
				}

				/* teleport all players back when the game ends */
				if (removeResult) game.online().forEach { disablePvp(it.uniqueId, it) }

				removeResult
			}
		}
	}

	fun teamPositions(game: PvpGame): List<Pair<Int, Int>> {
		val (centerX, centerZ) = game.getCenter()

		val radius = BORDER / 2 - 4

		val startAngle = Math.random() * PI * 2
		val angleStride = PI * 2 / game.teams.size

		return (game.teams.indices).map { i ->
			val angle = startAngle + angleStride * i
			Pair(centerX + (cos(angle) * radius).roundToInt(), centerZ + (sin(angle) * radius).roundToInt())
		}
	}

	fun destroyGames() {
		/* delete games and remove active players from them */
		ongoingGames.removeIf { game ->
			game.teams.forEach {
				team -> team.mapNotNull { Bukkit.getPlayer(it) }.forEach { disablePvp(it.uniqueId, it) }
			}
			true
		}
	}

	fun enablePvp(player: Player, save: Boolean, location: Location) {
		player.closeInventory()

		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* save before pvp state */
		if (save) playerData.lobbyInventory = player.inventory.contents.clone()

		Lobby.resetPlayerStats(player)
		player.gameMode = GameMode.SURVIVAL

		/* give items */
		val loadout = DataManager.loadouts.getPlayersLoadouts(player.uniqueId)[playerData.loadoutSlot.get()]
		loadout.fillInventory(player.inventory)

		NameManager.updateName(player)

		player.teleport(location)

		/* fake border */
		val game = playersGame(player.uniqueId) ?: return

		val border = WorldBorder()
		border.world = (WorldManager.getPVPWorld() as CraftWorld).handle
		val (centerX, centerZ) = game.getCenter()
		border.setCenter(centerX.toDouble(), centerZ.toDouble())
		border.size = game.borderSize.toDouble()

		player as CraftPlayer
		player.handle.b.sendPacket(ClientboundSetBorderCenterPacket(border))
		player.handle.b.sendPacket(ClientboundSetBorderSizePacket(border))
	}

	fun disablePvp(uuid: UUID, player: Player? = null) {
		val game = playersGame(uuid)
		game?.teams?.any { team -> team.removeIf { it == uuid } }

		/* if the player is online */
		val player = player ?: (Bukkit.getPlayer(uuid) ?: return)

		val playerData = PlayerData.getPlayerData(player.uniqueId)

		Lobby.onSpawnLobby(player)

		player.inventory.contents = playerData.lobbyInventory
	}
}
