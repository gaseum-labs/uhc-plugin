package com.codeland.uhc.blockfix

import com.codeland.uhc.core.UHC
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

class NetherWartFix : BlockFix("Nether Wart", arrayOf(
	Range("Wart", "wartCount", "wartIndex", 1, { ItemStack(Material.NETHER_WART) }, { ItemStack(Material.NETHER_WART) })
)) {
	override fun reject(uhc: UHC, tool: ItemStack, drops: List<Item>): Boolean {
		return false
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(material: Material): Boolean {
		return material == Material.NETHER_WART
	}
}
