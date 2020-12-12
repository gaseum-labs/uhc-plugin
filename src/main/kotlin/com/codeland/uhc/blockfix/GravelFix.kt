package com.codeland.uhc.blockfix

import com.codeland.uhc.core.UHC
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

class GravelFix : BlockFix("Gravel", arrayOf(
	Range("Flint", "gravelCount", "gravelIndex", 10, { ItemStack(Material.FLINT) }, { ItemStack(Material.GRAVEL) })
)) {
	override fun reject(uhc: UHC, tool: ItemStack, drops: List<Item>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(material: Material): Boolean {
		return material == Material.GRAVEL
	}
}
