package com.codeland.uhc.blockfix.blockfix

import com.codeland.uhc.blockfix.BlockFix
import com.codeland.uhc.blockfix.SingleRange
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack

class MelonFix : BlockFix("Melon", arrayOf(
	SingleRange("Slice") { _, _, fortune -> ItemStack(Material.MELON_SLICE, 1 + fortune) }
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return drops.firstOrNull()?.type === Material.MELON
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(blockState: BlockState): Boolean {
		return blockState.type === Material.MELON
	}
}
