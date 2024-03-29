package org.gaseumlabs.uhc.blockfix.blockfix

import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.blockfix.CountingRange

class GravelFix : org.gaseumlabs.uhc.blockfix.BlockFix("Gravel", arrayOf(
	CountingRange("Flint", 10, { _, _ -> ItemStack(Material.FLINT) }, { _, _ -> ItemStack(Material.GRAVEL) })
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(blockState: BlockState): Boolean {
		return blockState.type === Material.GRAVEL
	}
}
