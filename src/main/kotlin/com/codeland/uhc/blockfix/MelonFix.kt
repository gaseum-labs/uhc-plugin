package com.codeland.uhc.blockfix

import com.codeland.uhc.core.UHC
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

class MelonFix : BlockFix("Melon", arrayOf(
	Range("Melon Slice", "sliceCount", "sliceIndex", 1, { ItemStack(Material.MELON_SLICE) }, { ItemStack(Material.MELON_SLICE) })
)) {
	override fun reject(uhc: UHC, tool: ItemStack, drops: List<Item>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(material: Material): Boolean {
		return material == Material.MELON
	}
}
