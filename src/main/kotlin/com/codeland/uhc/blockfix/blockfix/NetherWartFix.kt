package com.codeland.uhc.blockfix.blockfix

import com.codeland.uhc.blockfix.BlockFix
import com.codeland.uhc.blockfix.SingleRange
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.block.data.Ageable
import org.bukkit.inventory.ItemStack

class NetherWartFix : BlockFix("Nether Wart", arrayOf(
	SingleRange("Wart") { _, _, fortune -> ItemStack(Material.NETHER_WART, 1 + fortune) }
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return false
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(blockState: BlockState): Boolean {
		return blockState.type === Material.NETHER_WART && (blockState.blockData as Ageable).age == 3
	}
}
