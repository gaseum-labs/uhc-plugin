package com.codeland.uhc.core

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.collections.ArrayList

enum class KillReward(val prettyName: String, val representation: Material, val lore: Array<String>, val apply: (UUID, ArrayList<UUID>) -> Unit) {
	ABSORPTION("Absorption", Material.GOLDEN_APPLE, arrayOf(
		"Gain 3 absorption hearts on kill",
		"Increased to 4 if alone",
		"1 absorption heart to teammates"
	), { player, others ->
		forPlayer(player, others) { player, onTeam -> player.absorptionAmount += if (onTeam) 6 else 8 }
		others.mapNotNull { Bukkit.getPlayer(it) }.forEach { it.absorptionAmount += 2 }
	}),
	REGENERATION("Regeneration", Material.GHAST_TEAR, arrayOf(
		"Regain 3 hearts on kill",
		"Increased to 4 if alone",
		"1 heart to teammates"
	), { player, others ->
		forPlayer(player, others) { player, onTeam ->
			player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, if (onTeam) 300 else 400, 0, false, true, true))
		}
		others.mapNotNull { Bukkit.getPlayer(it) }.forEach {
			it.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 100, 0, false, true, true))
		}
	}),
	NONE("None", Material.NETHER_WART, arrayOf(
		"No reward on kill"
	), { _, _ ->

	});

	companion object {
		fun forPlayer(uuid: UUID, others: ArrayList<UUID>, on: (Player, Boolean) -> Unit) {
			val player = Bukkit.getPlayer(uuid) ?: return
			on(player, others.contains(uuid))
		}
	}
}
