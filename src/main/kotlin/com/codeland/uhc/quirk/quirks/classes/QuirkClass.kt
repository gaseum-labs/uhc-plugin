package com.codeland.uhc.quirk.quirks.classes

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

enum class QuirkClass(val prettyName: String, val headBlock: Material, val onStart: (Player) -> Unit, val onEnd: (Player) -> Unit) {
	NO_CLASS("", Material.DIRT, {}, {}),

	LAVACASTER("Lavacaster", Material.MAGMA_BLOCK, { player ->
		player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, true))
	}, { player ->
		player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE)
	}),

	MINER("Miner", Material.DIAMOND_ORE, { player ->
		player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2, false, false, true))
		Classes.superBreakMap[player.uniqueId] = System.currentTimeMillis()
	}, { player ->
		player.removePotionEffect(PotionEffectType.FAST_DIGGING)
	}),

	HUNTER("Hunter", Material.WITHER_SKELETON_SKULL, {}, {}),

	ENCHANTER("Enchanter", Material.ENCHANTING_TABLE, {}, {}),

	DIVER("Diver", Material.PRISMARINE_BRICKS, { player ->
		player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false, true))
	}, { player ->
		player.removePotionEffect(PotionEffectType.WATER_BREATHING)
	}),

	TRAPPER("Trapper", Material.PISTON, {}, {})
}
