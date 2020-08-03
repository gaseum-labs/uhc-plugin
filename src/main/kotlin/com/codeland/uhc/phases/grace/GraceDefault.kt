package com.codeland.uhc.phases.grace

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.quirk.HalfZatoichi
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack
import kotlin.math.sqrt

open class GraceDefault : Phase() {

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

		Bukkit.getServer().dispatchCommand(uhc.gameMaster!!, String.format("spreadplayers 0 0 %f %f true @a", (uhc.startRadius / sqrt(teamCount.toDouble())), uhc.startRadius))
	}

	override fun perSecond(remainingSeconds: Int) {

	}

	override fun getCountdownString(): String {
		return "Grace period ends in"
	}

	override fun endPhrase(): String {
		return "GRACE PERIOD ENDING"
	}
}
