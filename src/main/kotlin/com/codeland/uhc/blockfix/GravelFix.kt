package com.codeland.uhc.blockfix

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class GravelFix : BlockFix("Gravel", arrayOf(
	Range("Flint", "gravelCount", "gravelIndex", 10, { _, _ -> ItemStack(Material.FLINT) }, { _, _ -> ItemStack(Material.GRAVEL) })
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(material: Material): Boolean {
		return material == Material.GRAVEL
	}
}
