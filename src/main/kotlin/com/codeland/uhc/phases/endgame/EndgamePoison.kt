package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class EndgamePoison : Phase() {
	override fun customStart() {}
	override fun customEnd() {}

	override fun onTick(currentTick: Int) {
		for (player in Bukkit.getServer().onlinePlayers) {
			player.health -= 0.05
		}
	}

	override fun perSecond(remainingSeconds: Int) {}

	override fun getCountdownString(): String {
		return ""
	}

	override fun endPhrase(): String {
		return ""
	}
}
