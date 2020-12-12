package com.codeland.uhc.blockfix

import com.codeland.uhc.core.UHC
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

class BrownMushroomFix : BlockFix("Brown mushroom block", arrayOf(
	Range("Mushroom", "mushroomCount", "mushroomIndex", 25, { ItemStack(Material.BROWN_MUSHROOM) })
)) {
	override fun reject(uhc: UHC, tool: ItemStack, drops: List<Item>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(block: Material): Boolean {
		return block == Material.BROWN_MUSHROOM_BLOCK
	}
}
