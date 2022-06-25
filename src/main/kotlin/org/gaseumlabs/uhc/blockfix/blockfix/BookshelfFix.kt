package org.gaseumlabs.uhc.blockfix.blockfix

import org.bukkit.Material
import org.bukkit.Material.BOOKSHELF
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.blockfix.SingleRange

class BookshelfFix : org.gaseumlabs.uhc.blockfix.BlockFix("Bookshelf", arrayOf(
	SingleRange("Planks") { _, _, _ -> ItemStack(Material.OAK_PLANKS, 6) },
	SingleRange("books") { _, _, _ -> ItemStack(Material.BOOK, 3) },
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	override fun isBlock(blockState: BlockState): Boolean {
		return blockState.type === BOOKSHELF
	}
}
