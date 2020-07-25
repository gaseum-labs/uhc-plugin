package com.codeland.uhc.phases.glowing

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class GlowingTopTwo : Phase() {

	override fun customStart() {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendPlayer(player, "Glowing will be applied to the top two teams")
		}
	}

	override fun perSecond(second: Int) {
		val sortedTeams = Bukkit.getServer().scoreboardManager.mainScoreboard.teams.sortedByDescending {
			var ret = 0.0
			for (entry in it.entries) {
				val player = Bukkit.getServer().getPlayer(entry)
				if (player != null) {
					if (player.gameMode == GameMode.SURVIVAL) {
						ret += player.health + player.absorptionAmount
					}
				}
			}
			return@sortedByDescending ret
		}

		sortedTeams.forEachIndexed { i, team ->
			if (i < 2) {
				for (entry in team.entries) {
					Bukkit.getServer().getPlayer(entry)?.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 0, false, false, false))
				}
			} else {
				for (entry in team.entries) {
					Bukkit.getServer().getPlayer(entry)?.removePotionEffect(PotionEffectType.GLOWING)
				}
			}
		}
	}

	override fun getCountdownString(): String {
		return "Endgame starts in"
	}

	override fun endPhrase(): String {
		return "ENDGAME STARTING"
	}
}
