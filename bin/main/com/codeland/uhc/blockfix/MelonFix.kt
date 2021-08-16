package com.codeland.uhc.blockfix

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class MelonFix : BlockFix("Melon", arrayOf(
	Range.nonCountRange { _, _ -> ItemStack(Material.MELON_SLICE) }
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return drops.firstOrNull()?.type === Material.MELON
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(material: Material): Boolean {
		return material === Material.MELON
	}
}
