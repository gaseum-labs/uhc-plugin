package com.codeland.uhc.phases.glowing

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class GlowingDefault : Phase() {

	override fun customStart() {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendPlayer(player, "Glowing has been applied")
		}

		for (player in Bukkit.getServer().onlinePlayers) {
			if (player.gameMode == GameMode.SURVIVAL) {
				player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 1, false, false, false))
			}
		}
	}

	override fun perSecond(remainingSeconds: Int) {

	}

	override fun getCountdownString(): String {
		return "Endgame starts in"
	}

	override fun endPhrase(): String {
		return "ENDGAME STARTING"
	}
}
