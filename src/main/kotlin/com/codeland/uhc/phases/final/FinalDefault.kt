package com.codeland.uhc.phases.final

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit

class FinalDefault : Phase() {
	override fun start(uhc: UHC, length: Long) {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendPlayer(player, "The border has stopped shrinking")
		}
	}

	override fun getCountdownString(): String {
		return "Glowing starts in "
	}

	override fun endPhrase(): String {
		return "GLOWING WILL BE APPLIED"
	}

}