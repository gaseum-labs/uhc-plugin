package com.codeland.uhc.phases.glowing

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.UHCPhase
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class GlowingDefault : Phase() {

	override fun start(uhc: UHC, length: Long) {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendPlayer(player, "Glowing has been applied")
		}

		for (player in Bukkit.getServer().onlinePlayers) {
			if (player.gameMode == GameMode.SURVIVAL) {
				player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 1, false, false, false))
			}
		}

		if (length > 0) {//no endgame
			super.start(uhc, length)
		}
	}

	override fun getCountdownString(): String {
		return "Endgame starts in "
	}

	override fun getPhaseType(): UHCPhase {
		return UHCPhase.GLOWING
	}

	override fun endPhrase(): String {
		return "ENDGAME STARTING"
	}

}