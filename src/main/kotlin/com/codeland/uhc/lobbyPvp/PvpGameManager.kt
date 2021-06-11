package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.event.Packet
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object PvpGameManager {
	const val ARENA_STRIDE = 160
	const val BEACH = 128
	const val LARGE_BORDER = 96
	const val SMALL_BORDER = 64

	class PvpGame(val players: ArrayList<UUID>, position: Int, val borderSize: Int) {
		val x = xFromPosition(position)
		val z = zFromPosition(position)

		var winner: String? = null
		var time = -4

		var glowPeriod = 60
		var glowTimer = glowPeriod

		fun onlinePlayers() = players.mapNotNull { Bukkit.getPlayer(it) }

		fun centerLocation(): Pair<Int, Int> {
			return Pair(x * ARENA_STRIDE + (ARENA_STRIDE / 2), z * ARENA_STRIDE + (ARENA_STRIDE / 2))
		}

		fun alive(): List<Player> {
			return players.mapNotNull { Bukkit.getPlayer(it) }
				.filter { it.location.world.name == WorldManager.PVP_WORLD_NAME && it.gameMode != GameMode.SPECTATOR }
		}

		fun endNaturally(winner: String) {
			this.winner = winner
			time = -10

			players.mapNotNull { Bukkit.getPlayer(it) }.forEach { it.sendTitle("${ChatColor.RED}$winner wins!", "", 0, 160, 40) }
		}

		fun checkEnd(): Boolean {
			val alive = alive()

			return if (alive.size == 1) {
				endNaturally(alive.first().name)
				false

			/* if somehow both players disconnect then end it immediately */
			} else alive.isEmpty()
		}

		fun prepareArena() {
			val world = WorldManager.getPVPWorld()
			val border = (ARENA_STRIDE - LARGE_BORDER) / 2

			for (bx in x * ARENA_STRIDE + border until x * ARENA_STRIDE + ARENA_STRIDE - border) {
				for (bz in z * ARENA_STRIDE + border until z * ARENA_STRIDE + ARENA_STRIDE - border) {
					/* bedrock floor below sea level */
					val aboveBlock = world.getBlockAt(bx, 62, bz)

					if (aboveBlock.type.isAir || aboveBlock.isLiquid) {
						world.getBlockAt(bx, 61, bz).setType(Material.SAND, false)
						world.getBlockAt(bx, 60, bz).setType(Material.BEDROCK, false)
					} else {
						world.getBlockAt(bx, 61, bz).setType(Material.BEDROCK, false)
					}

					/* super mountain clearer */
					for (by in 128..255) world.getBlockAt(bx, by, bz).setType(Material.AIR, false)
				}
			}
		}

		fun shouldGlow(): Boolean {
			return glowTimer <= 0
		}

		fun updateGlowAll() {
			val online = onlinePlayers()

			online.forEach { player1 ->
				online.forEach { player2 ->
					(player1 as CraftPlayer).handle.playerConnection.sendPacket(Packet.metadataPacketDefaultState(player2))
				}
			}
		}

		fun isOver(): Boolean {
			return winner != null
		}
	}

	val ongoingGames = ArrayList<PvpGame>()
	var nextGamePosition = 0

	fun addGame(players: ArrayList<UUID>) {
		val game = PvpGame(players, nextGamePosition, SMALL_BORDER)

		val playerData0 = PlayerData.getPlayerData(game.players[0])
		playerData0.lastPlayed = game.players[1]
		val playerData1 = PlayerData.getPlayerData(game.players[1])
		playerData1.lastPlayed = game.players[0]

		game.prepareArena()

		++nextGamePosition
		ongoingGames.add(game)
	}

	fun playersGame(uuid: UUID): PvpGame? {
		return ongoingGames.find { game -> game.players.contains(uuid) }
	}

	fun perTick(currentTick: Int) {
		if (currentTick % 4 == 0) {
			val arrows = WorldManager.getPVPWorld().getEntitiesByClass(AbstractArrow::class.java)

			arrows.forEach { arrow ->
				val x = Util.mod(arrow.location.blockX, ARENA_STRIDE)
				val z = Util.mod(arrow.location.blockZ, ARENA_STRIDE)

				if (x < 32 || x > ARENA_STRIDE - 32 || z < 32 || z > ARENA_STRIDE - 32) arrow.remove()
			}
		}

		if (currentTick % 20 == 0) {
			PvpQueue.perSecond()

			ongoingGames.removeIf { game ->
				++game.time

				/* postgame length until game is destroyed */
				val removeResult = if (game.isOver()) {
					game.time >= 0

				/* before and during the game */
				} else when {
					/* countdown before match starts */
					game.time < 0 -> {
						game.onlinePlayers().forEach { player ->
							player.sendTitle("${ChatColor.RED}${-game.time}", "${ChatColor.RED}PVP Match Starting", 0, 21, 0)
							player.sendActionBar(Component.text(""))
						}

						false
					}
					/* start match */
					game.time == 0 -> {
						val world = WorldManager.getPVPWorld()

						val positions = playerPositions(game).map { position ->
							val (liquidY, solidY) = Util.topLiquidSolidY(world, position.first, position.second)
							Location(world, position.first + 0.5, (if (liquidY == -1) solidY else liquidY) + 1.0, position.second + 0.5)
						}

						val online = game.onlinePlayers()
						val data = online.zip(positions)

						if (data.size < game.players.size) {
							online.forEach { player -> Commands.errorMessage(player, "Game cancelled! A player left") }
							true

						} else {
							data.forEach { (player, position) ->
								enablePvp(player, true, position)
								player.sendTitle("${ChatColor.GOLD}FIGHT", "", 0, 20, 10)
							}
							false
						}
					}
					/* during match */
					else -> {
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
				if (removeResult) game.onlinePlayers().forEach { disablePvp(it) }

				removeResult
			}
		}
	}

	fun playerPositions(game: PvpGame): List<Pair<Int, Int>> {
		val (centerX, centerZ) = game.centerLocation()

		val radius = SMALL_BORDER / 2 - 4

		val startAngle = Math.random() * PI * 2
		val angleStride = PI * 2 / game.players.size

		return (game.players.indices).map { i ->
			val angle = startAngle + angleStride * i
			Pair(centerX + (cos(angle) * radius).roundToInt(), centerZ + (sin(angle) * radius).roundToInt())
		}
	}

	private fun xFromPosition(position: Int): Int {
		return position % 16
	}

	private fun zFromPosition(position: Int): Int {
		return position / 16
	}

	fun removePlayerFromGame(uuid: UUID) {
		val game = playersGame(uuid)
		game?.players?.remove(uuid)
	}

	fun enablePvp(player: Player, save: Boolean, location: Location) {
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		/* save before pvp state */
		if (save) playerData.lobbyInventory = player.inventory.contents.clone()

		Lobby.resetPlayerStats(player)
		player.gameMode = GameMode.SURVIVAL

		/* give items */
		val loadout = DataManager.loadouts.getLoadouts(player.uniqueId)[playerData.loadoutSlot.get()]
		LoadoutItems.fillInventory(loadout, player.inventory)

		player.teleport(location)
	}

	fun disablePvp(player: Player) {
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		Lobby.onSpawnLobby(player)

		player.inventory.contents = playerData.lobbyInventory
	}
}
