package com.codeland.uhc.phases.grace

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Ledger
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack
import kotlin.math.*

open class GraceDefault : Phase() {

	/* to be injected on phase creation */
	lateinit var teleportLocations: ArrayList<Location>

	override fun customStart() {
		Bukkit.getServer().onlinePlayers.forEach {
			it.inventory.clear()
			it.setItemOnCursor(null)

			for (activePotionEffect in it.activePotionEffects)
				it.removePotionEffect(activePotionEffect.type)

			it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: 20.0
			it.absorptionAmount = 0.0
			it.exp = 0f
			it.level = 0
			it.foodLevel = 20
			it.saturation = 5f
			it.exhaustion = 0f

			it.setStatistic(Statistic.TIME_SINCE_REST,
				if (GameRunner.uhc.isEnabled(QuirkType.MODIFIED_DROPS)) 72000
				else 0
			)

			if (GameRunner.uhc.isEnabled(QuirkType.WET_SPONGE))
				it.inventory.addItem(ItemStack(Material.WET_SPONGE, 1))

			it.gameMode = if (GameRunner.playersTeam(it.name) == null) GameMode.SPECTATOR else GameMode.SURVIVAL
		}

		for (w in Bukkit.getServer().worlds) {
			w.setGameRule(GameRule.DO_MOB_SPAWNING, true)
			w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
			w.worldBorder.setCenter(0.0, 0.0)
			w.worldBorder.size = uhc.startRadius * 2
		}

		val teamCount = 1 + Bukkit.getServer().scoreboardManager.mainScoreboard.teams.size

		Bukkit.getServer().scoreboardManager.mainScoreboard.teams.forEach { team ->
			team.entries.forEachIndexed { i, entry ->
				val player = Bukkit.getPlayer(entry)

				player?.teleportAsync(teleportLocations[i])?.thenAccept {
					player?.fallDistance = 0f
				}
			}
		}

		/* reset the ledger */
		uhc.elapsedTime = 0
		uhc.ledger = Ledger()

		/* begin care packages */
		if (uhc.carePackages.enabled) {
			uhc.carePackages.onStart()
		}
	}

	override fun perSecond(remainingSeconds: Int) {

	}

	override fun getCountdownString(): String {
		return "Grace period ends in"
	}

	override fun endPhrase(): String {
		return "GRACE PERIOD ENDING"
	}

	companion object {
		fun spreadSinglePlayer(world: World, spreadRadius: Double): Location? {
			for (i in 0 until 16) {
				val location = findLocation(world, Math.random() * 2 * Math.PI, Math.PI * 0.9, spreadRadius)
				if (location != null) return location
			}

			return null
		}

		fun spreadPlayers(world: World, numTeams: Int, spreadRadius: Double): ArrayList<Location> {
			val ret = ArrayList<Location>(numTeams)

			var angle = Math.random() * 2 * Math.PI

			val angleAdvance = 2 * Math.PI / numTeams
			val angleDeviation = angleAdvance / 4

			for (i in 0 until numTeams) {
				val location = findLocation(world, angle, angleDeviation, spreadRadius) ?: return ArrayList()
				ret.add(location)

				angle += angleAdvance
			}

			return ret
		}

		fun findLocation(world: World, angle: Double, angleDeviation: Double, spreadRadius: Double): Location? {
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
				val (liquidY, solidY) = Util.topLiquidSolidY(world, x, z)

				/* if this there is no liquid or void in this block */
				if (solidY != -1) return Location(world, x + 0.5, solidY.toDouble() + 1, z + 0.5)

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
