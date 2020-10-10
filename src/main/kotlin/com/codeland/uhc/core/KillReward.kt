package com.codeland.uhc.core

import com.codeland.uhc.team.Team
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

enum class KillReward(val prettyName: String, val representation: Material, val lore: List<String>) {
	REGENERATION("Regeneration", Material.GHAST_TEAR, listOf("Kill a team to have your team each regenerate 2 hearts")),
	STRENGTH("Strength", Material.BLAZE_POWDER, listOf("Kill a team to have your team get strength for 5 minutes")),
	NONE("None", Material.NETHER_WART, listOf("No reward for killing a team"));

	fun applyReward(players: Array<Player?>) {
		when (this) {
			REGENERATION -> players.forEach { member ->
				member?.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 200, 0, false, true, true))
			}
			STRENGTH ->	players.forEach { member ->
				member?.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20 * 60, 0, false, true, true))
			}
		}
	}
}
