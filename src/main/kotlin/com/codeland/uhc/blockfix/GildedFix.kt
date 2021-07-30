package com.codeland.uhc.blockfix

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class GildedFix : BlockFix("Gilded", arrayOf(
	Range.nonCountRange { _, _ -> ItemStack(Material.GOLD_NUGGET, 4) }
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return when (tool.type) {
			Material.WOODEN_PICKAXE,
			Material.STONE_PICKAXE,
			Material.IRON_PICKAXE,
			Material.GOLDEN_PICKAXE,
			Material.DIAMOND_PICKAXE,
			Material.NETHERITE_PICKAXE -> true
			else -> false
		}
	}

	override fun isBlock(material: Material): Boolean {
		return material === Material.NETHER_GOLD_ORE || material === Material.GILDED_BLACKSTONE
	}
}
