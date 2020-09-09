package com.codeland.uhc.phaseType

import org.bukkit.Bukkit
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team

enum class KillReward {
	STRENGTH,
	NONE;
	fun applyReward(team : Team) {
		if (this == STRENGTH) {
			for (entry in team.entries) {
				Bukkit.getServer().getPlayer(entry)?.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20 * 60, 0, false, true, true))
			}
		}
	}
}