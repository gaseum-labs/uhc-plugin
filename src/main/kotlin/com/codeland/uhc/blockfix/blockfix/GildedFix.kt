package com.codeland.uhc.blockfix.blockfix

import com.codeland.uhc.blockfix.BlockFix
import com.codeland.uhc.blockfix.SingleRange
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack

class GildedFix : BlockFix("Gilded", arrayOf(
	SingleRange("Nuggets") { _, _, fortune -> ItemStack(Material.GOLD_NUGGET, 4 + fortune) }
)) {
	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return when (tool.type) {
			Material.WOODEN_PICKAXE,
			Material.STONE_PICKAXE,
			Material.IRON_PICKAXE,
			Material.GOLDEN_PICKAXE,
			Material.DIAMOND_PICKAXE,
			Material.NETHERITE_PICKAXE -> true
			else -> false
		}
	}

	override fun isBlock(blockState: BlockState): Boolean {
		return blockState.type === Material.NETHER_GOLD_ORE || blockState.type === Material.GILDED_BLACKSTONE
	}
}
