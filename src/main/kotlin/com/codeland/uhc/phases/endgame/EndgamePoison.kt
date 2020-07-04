package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.UHCPhase
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class EndgamePoison : Phase() {
	override fun start(uhc: UHC, length: Long) {
		runnable = object : BukkitRunnable() {
			override fun run() {
				for (player in Bukkit.getServer().onlinePlayers) {
					player.health -= 0.5
				}
			}
		}
		runnable?.runTaskTimer(GameRunner.plugin!!, 0, 10)
	}

	override fun getCountdownString(): String {
		return ""
	}

	override fun getPhaseType(): UHCPhase {
		return UHCPhase.ENDGAME
	}

	override fun endPhrase(): String {
		return ""
	}
}