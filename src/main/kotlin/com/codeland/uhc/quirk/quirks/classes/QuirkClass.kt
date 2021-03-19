package com.codeland.uhc.quirk.quirks.classes

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

enum class QuirkClass(val prettyName: String, val headBlock: Material, val onStart: (Player) -> Unit) {
	NO_CLASS("", Material.DIRT, {}),

	LAVACASTER("Lavacaster", Material.MAGMA_BLOCK, { player ->
		player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, true))
	}),

	MINER("Miner", Material.DIAMOND_ORE, { player ->
		player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2, false, false, true))
	}),

	HUNTER("Hunter", Material.WITHER_SKELETON_SKULL, {

	}),

	ALCHEMIST("Alchemist", Material.RED_STAINED_GLASS, {

	}),

	ENCHANTER("Enchanter", Material.ENCHANTING_TABLE, {

	}),

	DIVER("Diver", Material.PRISMARINE_BRICKS, { player ->
		player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false, true))
	}),

	TRAPPER("Trapper", Material.PISTON, {

	})
}
