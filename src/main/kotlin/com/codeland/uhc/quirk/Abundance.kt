package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.EventListener
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object Abundance {
	val leaves = arrayOf(
		Material.OAK_LEAVES,
		Material.ACACIA_LEAVES,
		Material.SPRUCE_LEAVES,
		Material.ACACIA_LEAVES,
		Material.BIRCH_LEAVES,
		Material.JUNGLE_LEAVES,
		Material.DARK_OAK_LEAVES
	)

	init {
		leaves.sort()
	}

	fun extraDrops(block: Block, drops: MutableCollection<ItemStack>) {
		if (GameRunner.binarySearch(block.type, leaves)) {
			if (drops.find { drop -> drop.type == Material.APPLE } == null) {
				if (Math.random() < 0.1)
					drops.add(ItemStack(Material.APPLE))
			}
		}
	}
}