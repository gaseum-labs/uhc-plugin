package com.codeland.uhc.phases.grace

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Zatoichi
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.quirk.Quirk
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.attribute.Attribute
import kotlin.math.sqrt

open class GraceDefault : Phase() {

	override fun customStart() {
		Bukkit.getServer().onlinePlayers.forEach {
			it.inventory.clear()
			it.setItemOnCursor(null)
			for (activePotionEffect in it.activePotionEffects) {
				it.removePotionEffect(activePotionEffect.type)
			}
			it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: 20.0
			it.absorptionAmount = 0.0
			it.exp = 0.0F
			it.level = 0
			if (GameRunner.playersTeam(it.name) != null) {
				it.gameMode = GameMode.SURVIVAL
			} else {
				it.gameMode = GameMode.SPECTATOR
			}
		}

		for (w in Bukkit.getServer().worlds) {
			w.setGameRule(GameRule.NATURAL_REGENERATION, true)
			w.setGameRule(GameRule.DO_MOB_SPAWNING, true)
			w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
			w.worldBorder.setCenter(0.0, 0.0)
			w.worldBorder.size = uhc.startRadius * 2
			w.pvp = false
		}

		val teamCount = 1 + Bukkit.getServer().scoreboardManager.mainScoreboard.teams.size

		Bukkit.getServer().dispatchCommand(uhc.gameMaster!!, String.format("spreadplayers 0 0 %f %f true @a", (uhc.startRadius / sqrt(teamCount.toDouble())), uhc.startRadius))

		if (Quirk.HALF_ZATOICHI.enabled)
			Zatoichi.start(uhc, length)
	}

	override fun perSecond(remainingSeconds: Long) {
		TODO("Not yet implemented")
	}

	override fun getCountdownString(): String {
		return "Grace period ends in "
	}

	override fun endPhrase(): String {
		return "GRACE PERIOD ENDING"
	}
}