package com.codeland.uhc.phase.phases.grace

import com.codeland.uhc.core.*
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import org.bukkit.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

class GraceDefault : Phase() {
	override fun customStart() {
		/* set border in each game dimension */
		listOf(WorldManager.getGameWorldGame(), WorldManager.getNetherWorldGame()).forEach { world ->
			world.worldBorder.setCenter(0.5, 0.5)
			world.worldBorder.size = UHC.startRadius * 2 + 1.0

			world.time = 0
			world.isThundering = false
			world.setStorm(false)
		}

		val teleportGroups = UHC.teleportGroups ?: return
		val teleportLocations = UHC.teleportLocations ?: return

		/* give all teams that don't have names a name */
		/* add people to team vcs */
		TeamData.teams.forEach { team ->
			if (team.name == null) team.automaticName()
			if (UHC.usingBot.get()) GameRunner.bot?.addToTeamChannel(team, team.members)
		}

		/* teleport and set playerData to current */
		teleportGroups.forEachIndexed { i, teleportGroup ->
			teleportGroup.forEach { uuid -> startPlayer(uuid, teleportLocations[i]) }
		}

		/* reset the ledger */
		UHC.elapsedTime = 0
		UHC.ledger = Ledger()
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Float {
		return barLengthRemaining(remainingSeconds, currentTick)
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		UHC.updateMobCaps()
		UHC.containSpecs()
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return "${ChatColor.RESET}Grace period ends in ${phaseType.chatColor}${ChatColor.BOLD}${Util.timeString(remainingSeconds)}"
	}

	override fun endPhrase(): String {
		return "Grace Period Ending"
	}

	companion object {
		fun startPlayer(uuid: UUID, location: Location) {
			val playerData = PlayerData.getPlayerData(uuid)

			playerData.staged = false
			playerData.alive = true
			playerData.participating = true

			PvpGameManager.removePlayerFromGame(uuid)

			GameRunner.teleportPlayer(uuid, location)

			GameRunner.playerAction(uuid) { player ->
				Lobby.resetPlayerStats(player)

				/* remove all advancements */
				Bukkit.getServer().advancementIterator().forEach { advancement ->
					val progress = player.getAdvancementProgress(advancement)

					progress.awardedCriteria.forEach { criteria -> progress.revokeCriteria(criteria) }
				}

				player.gameMode = GameMode.SURVIVAL
			}

			UHC.quirks.forEach { quirk -> if (quirk.enabled.get()) quirk.onStart(uuid) }
		}

		fun spreadSinglePlayer(world: World, spreadRadius: Double): Location? {
			for (i in 0 until 16) {
				val location = findLocation(world, Math.random() * 2 * Math.PI, Math.PI * 0.9, spreadRadius, if (world.environment == World.Environment.NETHER) ::findYMid else ::findYTop)
				if (location != null) return location
			}

			return null
		}

		/**
		 * @return an empty arraylist if not all spaces could be filled
		 */
		fun spreadPlayers(world: World, numSpaces: Int, spreadRadius: Double, findY: (World, Int, Int) -> Int): ArrayList<Location> {
			val ret = ArrayList<Location>(numSpaces)

			var angle = Math.random() * 2 * Math.PI

			val angleAdvance = 2 * Math.PI / numSpaces
			val angleDeviation = angleAdvance / 4

			for (i in 0 until numSpaces) {
				val location = findLocation(world, angle, angleDeviation, spreadRadius, findY) ?: return ArrayList()
				ret.add(location)

				angle += angleAdvance
			}

			return ret
		}

		fun findYTop(world: World, x: Int, z: Int): Int {
			return Util.topLiquidSolidY(world, x, z).second
		}

		/**
		 * used for finding a spawn y value at a certain x, z position
		 * in chunks using nether noise generation
		 *
		 * @return a y value to spawn the player at, -1 if no good value
		 * could be found
		 */
		fun findYMid(world: World, x: Int, z: Int): Int {
			val chunk = world.getChunkAt(Location(world, x.toDouble(), 0.0, z.toDouble()))
			val subX = Util.mod(x, 16)
			val subZ = Util.mod(z, 16)

			val low = 30
			val high = 100
			val height = high - low

			val offset = Util.randRange(0, height)

			for (y in 0..height) {
				val usingY = (y + offset) % height + low

				if (
					 chunk.getBlock(subX, usingY + 2, subZ).isEmpty &&
					 chunk.getBlock(subX, usingY + 1, subZ).isEmpty &&
					!chunk.getBlock(subX, usingY    , subZ).isPassable
				) return usingY
			}

			return -1
		}

		fun findLocation(world: World, angle: Double, angleDeviation: Double, spreadRadius: Double, findY: (World, Int, Int) -> Int): Location? {
			val minRadius = spreadRadius / 2

			/* initial (i, j) within the 32 * 32 polar coordinate area is random */
			var position = (Math.random() * 32 * 32).toInt()
			for (iterator in 0 until 32 * 32) {
				val i = position % 32
				val j = position / 32

				/* convert to polar coordinates to search for valid spots */
				val iAngle = (angleDeviation * 2) * (i / 32.0) + (angle - angleDeviation)
				val jRadius = (spreadRadius - minRadius) * (j / 32.0) + minRadius

				val squircleRadius = getSquircleRadius(iAngle, jRadius)

				val x = round(cos(iAngle) * squircleRadius).toInt()
				val z = round(sin(iAngle) * squircleRadius).toInt()
				val y = findY(world, x, z)

				/* if the y check is good then use this position */
				if (y != -1) return Location(world, x + 0.5, y.toDouble() + 1, z + 0.5)

				position = (position + 1) % (32 * 32)
			}

			return null
		}

		fun positiveMod(a: Double, b: Double): Double {
			return (a % b + b) % b
		}

		fun getSquareRadius(angle: Double): Double {
			val angle = positiveMod(angle, Math.PI * 2)

			return when {
				angle < (Math.PI / 4) -> 1 / Math.cos(angle)
				angle < (3 * Math.PI / 4) -> 1 / Math.sin(angle)
				angle < (5 * Math.PI / 4) -> -1 / Math.cos(angle)
				angle < (7 * Math.PI / 4) -> -1 / Math.sin(angle)
				else -> 1 / Math.cos(angle)
			}
		}

		fun getSquircleRadius(angle: Double, radius: Double): Double {
			return (getSquareRadius(angle) * radius + radius) / 2
		}
	}
}
