package com.codeland.uhc.phase.phases.grace

import com.codeland.uhc.core.CustomSpawning
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Ledger
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import kotlin.collections.ArrayList
import kotlin.math.*

open class GraceDefault : Phase() {
	override fun customStart() {
		/* set border in overworld */
		Bukkit.getWorlds().forEach { world ->
			world.time = 0

			if (world.environment == uhc.defaultEnvironment) {
				world.worldBorder.setCenter(0.5, 0.5)
				world.worldBorder.size = uhc.startRadius * 2 + 1.0
			} else {
				world.worldBorder.reset()
			}
		}

		val teleportGroups = uhc.teleportGroups ?: return
		val teleportLocations = uhc.teleportLocations ?: return

		/* teleport and set players */
		teleportGroups.forEachIndexed { i, teleportGroup ->
			teleportGroup.forEach { uuid ->
				GameRunner.uhc.setAlive(uuid, true)

				GameRunner.teleportPlayer(uuid, teleportLocations[i])
				GameRunner.playerAction(uuid, ::startPlayer)
				GameRunner.uhc.quirks.forEach { quirk ->
					if (quirk.enabled) quirk.onStart(uuid)
				}
			}
		}

		/* non participants into spec */
		Bukkit.getOnlinePlayers().forEach { player ->
			if (!GameRunner.uhc.isParticipating(player.uniqueId)) {
				player.gameMode = GameMode.SPECTATOR
				player.teleport(GameRunner.uhc.spectatorSpawnLocation())
			}
		}

		/* reset the ledger */
		uhc.elapsedTime = 0
		uhc.ledger = Ledger()
	}

	private fun startPlayer(player: Player) {
		/* absolutely nuke the inventory */
		player.inventory.clear()
		player.itemOnCursor.amount = 0
		player.setItemOnCursor(null)

		/* clear crafting slots */
		player.openInventory.topInventory.clear()
		player.openInventory.bottomInventory.clear()

		for (activePotionEffect in player.activePotionEffects)
			player.removePotionEffect(activePotionEffect.type)
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
		player.health = 20.0
		player.absorptionAmount = 0.0
		player.exp = 0f
		player.level = 0
		player.foodLevel = 20
		player.saturation = 5f
		player.exhaustion = 0f
		player.fireTicks = -1
		player.fallDistance = 0f
		player.setStatistic(Statistic.TIME_SINCE_REST, 0)

		/* remove all advancements */
		Bukkit.getServer().advancementIterator().forEach { advancement ->
			val progress = player.getAdvancementProgress(advancement)

			progress.awardedCriteria.forEach { criteria ->
				progress.revokeCriteria(criteria)
			}
		}

		player.gameMode = GameMode.SURVIVAL

		CustomSpawning.startSpawning()
	}

	override fun customEnd() {}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return barLengthRemaining(remainingSeconds, currentTick)
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		uhc.updateMobCaps()
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return "${ChatColor.RESET}Grace period ends in ${phaseType.chatColor}${ChatColor.BOLD}${Util.timeString(remainingSeconds)}"
	}

	override fun endPhrase(): String {
		return "Grace Period Ending"
	}

	companion object {
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
