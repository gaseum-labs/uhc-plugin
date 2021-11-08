package com.codeland.uhc.quirk.quirks.classes

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

enum class QuirkClass(val prettyName: String, val headBlock: Material,
					  val onStart: (Player) -> Unit = {},
					  val headMeta: (ItemMeta) -> Unit = {},
					  val onEnd: (Player) -> Unit = {},
) {

	NO_CLASS("", Material.DIRT),

	LAVACASTER("Lavacaster", Material.MAGMA_BLOCK, { player ->
		player.addPotionEffect(PotionEffect(
				PotionEffectType.FIRE_RESISTANCE,
				Integer.MAX_VALUE, 0,
				false, false, true))
	}, {}, { player ->
		player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE)
	}),

	MINER("Miner", Material.DIAMOND_ORE, { player ->
		Classes.minerDatas[player.uniqueId] = Classes.MinerData(0)
	}, {}, { player ->
		Classes.minerDatas.remove(player.uniqueId)
	}),

	HUNTER("Hunter", Material.SPAWNER),

	ENCHANTER("Enchanter", Material.ENCHANTING_TABLE),

	DIVER("Diver", Material.PRISMARINE_BRICKS, { player ->
		player.addPotionEffect(PotionEffect(
				PotionEffectType.WATER_BREATHING,
				Integer.MAX_VALUE, 0,
				false, false, true))
	}, { itemMeta ->
		itemMeta.addEnchant(Enchantment.WATER_WORKER, 1, true)
	}, { player ->
		player.removePotionEffect(PotionEffectType.WATER_BREATHING)
	}),

	ENGINEER("Engineer", Material.PISTON)
}
