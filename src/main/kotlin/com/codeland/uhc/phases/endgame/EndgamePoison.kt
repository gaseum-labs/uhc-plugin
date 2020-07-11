package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class EndgamePoison : Phase() {

	var posionRunnable = null as BukkitRunnable?

	override fun customStart() {
		posionRunnable = object : BukkitRunnable() {
			override fun run() {
				for (player in Bukkit.getServer().onlinePlayers) {
					player.health -= 0.5
				}
			}
		}

		posionRunnable?.runTaskTimer(GameRunner.plugin!!, 0, 10)
	}

	override fun perSecond(remainingSeconds: Long) {

	}

	override fun onEnd() {
		super.onEnd()

		posionRunnable?.cancel()
	}

	override fun getCountdownString(): String {
		return ""
	}

	override fun endPhrase(): String {
		return ""
	}
}
