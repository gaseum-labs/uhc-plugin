package com.codeland.uhc.core

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

enum class KillReward(val prettyName: String, val representation: Material, val lore: List<Component>) {
	REGENERATION("Regeneration", Material.GHAST_TEAR, listOf(Component.text("Kill a team to have your team each regenerate 2 hearts"))),
	STRENGTH("Strength", Material.BLAZE_POWDER, listOf(Component.text("Kill a team to have your team get strength for 5 minutes"))),
	NONE("None", Material.NETHER_WART, listOf(Component.text("No reward for killing a team")));

	fun applyReward(players: ArrayList<Player?>) {
		when (this) {
			REGENERATION -> players.forEach { member ->
				member?.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 200, 0, false, true, true))
			}
			STRENGTH ->	players.forEach { member ->
				member?.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20 * 60, 0, false, true, true))
			}
			else -> {}
		}
	}
}
