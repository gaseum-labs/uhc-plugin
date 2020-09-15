package com.codeland.uhc.blockfix

import com.codeland.uhc.core.UHC
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

class RedMushroomFix : BlockFix("Red mushroom block", arrayOf(
	Range("Mushroom", "mushroomCount", "mushroomIndex", 50) { mushroomBlock -> ItemStack(Material.RED_MUSHROOM) }
)) {
	override fun reject(uhc: UHC, drops: List<Item>): Boolean {
		return !uhc.stewFix ||
		(drops.isNotEmpty() && drops[0].itemStack.type == Material.RED_MUSHROOM_BLOCK)
	}

	override fun allowTool(item: ItemStack): Boolean {
		return when (item.type) {
			Material.IRON_AXE -> true
			Material.DIAMOND_AXE -> true
			Material.NETHERITE_AXE -> true
			else -> false
		}
	}

	override fun isBlock(block: Material): Boolean {
		return block == Material.RED_MUSHROOM_BLOCK
	}
}
