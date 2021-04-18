package com.codeland.uhc.blockfix

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class NetherWartFix : BlockFix("Nether Wart", arrayOf(
	Range("Wart", "wartCount", "wartIndex", 1, { _, _ -> ItemStack(Material.NETHER_WART) }, { _, _ -> ItemStack(Material.NETHER_WART) })
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return false
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(material: Material): Boolean {
		return material == Material.NETHER_WART
	}
}
