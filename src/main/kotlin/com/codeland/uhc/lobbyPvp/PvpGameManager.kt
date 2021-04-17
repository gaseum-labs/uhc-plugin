package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
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
	const val BORDER_SIZE = 64

	class PvpGame(val players: Array<UUID>, position: Int) {
		val x = xFromPosition(position)
		val z = zFromPosition(position)

		var time = -4

		fun centerLocation(): Pair<Int, Int> {
			return Pair(x * ARENA_STRIDE + (ARENA_STRIDE / 2), z * ARENA_STRIDE + (ARENA_STRIDE / 2))
		}
	}

	val ongoingGames = ArrayList<PvpGame>()
	var nextGamePosition = 0

	fun addGame(players: Array<UUID>) {
		val game = PvpGame(players, nextGamePosition)

		++nextGamePosition
		ongoingGames.add(game)
	}

	fun playersGame(uuid: UUID): PvpGame? {
		return ongoingGames.find { game -> game.players.contains(uuid) }
	}

	fun perTick(currentTick: Int) {
		if (currentTick % 20 == 0) {
			PvpQueue.perSecond()

			ongoingGames.removeIf { game ->
				++game.time

				when {
					/* countdown before match starts */
					game.time < 0 -> {
						game.players.forEach { uuid ->
							Bukkit.getPlayer(uuid)?.sendTitle("${ChatColor.RED}${-game.time}", "${ChatColor.RED}PVP Match Starting", 0, 21, 0)
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

						val players = game.players.mapNotNull { Bukkit.getPlayer(it) }

						val data = players.zip(positions)
						if (data.size < game.players.size) {
							players.forEach { player -> Commands.errorMessage(player, "Game cancelled! A player left") }
							true

						} else {
							data.forEach { (player, position) ->
								PvpData.enablePvp(player, true, position)
								player.sendTitle("${ChatColor.GOLD}FIGHT", null, 0, 20, 10)
							}
							false
						}
					}
					/* during match */
					else -> {
						false
					}
				}
			}
		} else {
			false
		}
	}

	fun playerPositions(game: PvpGame): List<Pair<Int, Int>> {
		val (centerX, centerZ) = game.centerLocation()

		val radius = BORDER_SIZE / 2 - 4

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
}
