package com.codeland.uhc.phases.grace

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.UHCPhase
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRule
import kotlin.math.sqrt

open class GraceDefault : Phase() {

	override fun start(uhc : UHC, length : Long) {
		Bukkit.getServer().onlinePlayers.forEach {
			it.inventory.clear()
			it.setItemOnCursor(null)
			for (activePotionEffect in it.activePotionEffects) {
				it.removePotionEffect(activePotionEffect.type)
			}
			it.health = 20.0
			it.absorptionAmount = 0.0
			it.exp = 0.0F
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

		super.start(uhc, length)
	}

	override fun endPhase() {
		for (w in Bukkit.getServer().worlds) {
			w.setGameRule(GameRule.NATURAL_REGENERATION, false)
			w.pvp = true
		}
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendPlayer(player, "Grace period has ended!")
		}
	}

	override fun getCountdownString(): String {
		return "Grace period ends in "
	}

	override fun getPhaseType(): UHCPhase {
		return UHCPhase.GRACE
	}

	override fun endPhrase(): String {
		return "GRACE PERIOD ENDING"
	}
}