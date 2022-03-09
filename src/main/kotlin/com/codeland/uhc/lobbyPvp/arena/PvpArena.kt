package com.codeland.uhc.lobbyPvp.arena

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.event.Packet
import com.codeland.uhc.lobbyPvp.*
import org.bukkit.*
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.*

class PvpArena(teams: ArrayList<ArrayList<UUID>>, val matchType: Int) : Arena(ArenaType.PVP, teams) {
	companion object {
		const val TYPE_1V1 = 1
		const val TYPE_2V2 = 2

		fun typeName(type: Int) = if (type == TYPE_1V1) "1v1" else "2v2"

		fun load(data: String, world: World): Arena? {
			return null
		}
	}

	var winners: List<UUID> = ArrayList()

	var glowPeriod = 60
	var glowTimer = glowPeriod

	var pvpTimer = 0

	private fun endNaturally(winners: List<Player>) {
		this.winners = winners.map { it.uniqueId }
		pvpTimer = -10

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

	fun shouldGlow(): Boolean {
		return glowTimer <= 0 && !isOver()
	}

	fun updateGlowAll() {
		val online = online()

		online.forEach { player1 ->
			online.forEach { player2 ->
				(player1 as CraftPlayer).handle.connection.send(Packet.metadataPacketDefaultState(player2))
			}
		}
	}

	fun isOver(): Boolean {
		return winners.isNotEmpty()
	}

	/* override */

	override fun customPerSecond(): Boolean {
		++pvpTimer

		/* postgame length until game is destroyed */
		return if (isOver()) {
			pvpTimer >= 0

		} else {
			/* damage if outside the border */
			outsideBorder { player, outside ->
				player.damage(outside / 3.0)
			}

			if (glowPeriod > 0) {
				--glowTimer

				if (glowTimer == 0) {
					updateGlowAll()

				} else if (glowTimer <= -2) {
					glowPeriod /= 2
					glowTimer = glowPeriod
					updateGlowAll()
				}
			}

			checkEnd()
		}
	}

	override fun startingPositions(teams: ArrayList<ArrayList<UUID>>): List<List<Position>> {
		val (centerX, centerZ) = getCenter()

		val radius = ArenaManager.BORDER / 2.0f - ArenaManager.START_BUFFER

		val startAngle = (Math.random() * PI * 2).toFloat()
		val angleStride = PI.toFloat() * 2 / teams.size

		val teamAngle = 2 * asin((ArenaManager.TEAM_BUFFER / 2.0f) / radius)

		return (teams.indices).map { i ->
			val baseAngle = startAngle + angleStride * i

			teams[i].indices.map { j ->
				val angle = baseAngle + teamAngle * j
				val x = floor(centerX + (cos(angle) * radius)).toInt()
				val z = floor(centerZ + (sin(angle) * radius)).toInt()

				Position(x, z, angle * 180.0f)
			}
		}
	}

	override fun customStartPlayer(player: Player, playerData: PlayerData) {
		player.gameMode = GameMode.SURVIVAL

		/* give items */
		val loadout = UHC.dataManager.loadouts.getPlayersLoadouts(player.uniqueId)[playerData.loadoutSlot.get()]
		loadout.fillInventory(player.inventory)

		player.sendTitle("${ChatColor.GOLD}FIGHT", "", 0, 20, 10)
	}

	override fun prepareArena(world: World) {
		for (bx in x * ArenaManager.ARENA_STRIDE until (x + 1) * ArenaManager.ARENA_STRIDE) {
			for (bz in z * ArenaManager.ARENA_STRIDE until (z + 1) * ArenaManager.ARENA_STRIDE) {
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

	override fun startText() = "Entering PVP in"

	override fun shutdownOnLeave() = true

	override fun customSave(): String? = null
}
