package com.codeland.uhc.blockfix

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class BrownMushroomFix : BlockFix("Brown mushroom block", arrayOf(
	Range("Mushroom", "mushroomCount", "mushroomIndex", 25, { _, _ -> ItemStack(Material.BROWN_MUSHROOM) })
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return drops.firstOrNull()?.type == Material.BROWN_MUSHROOM_BLOCK
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(block: Material): Boolean {
		return block == Material.BROWN_MUSHROOM_BLOCK
	}
}
