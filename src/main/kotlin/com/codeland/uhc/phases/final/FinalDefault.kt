package com.codeland.uhc.phases.final

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit

class FinalDefault : Phase() {

	override fun customStart() {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendPlayer(player, "The border has stopped shrinking")
		}
	}

	override fun perSecond(remainingSeconds: Long) {

	}

	override fun getCountdownString(): String {
		return "Glowing starts in"
	}

	override fun endPhrase(): String {
		return "GLOWING WILL BE APPLIED"
	}
}
