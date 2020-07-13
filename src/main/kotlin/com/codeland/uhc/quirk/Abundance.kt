package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.AppleFix
import com.codeland.uhc.event.EventListener
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

object Abundance {
	fun extraDrops(block: Block, drops: MutableCollection<ItemStack>) {
		if (AppleFix.isLeaves(block)) {
			if (drops.find { drop -> drop.type == Material.APPLE } == null) {
				if (Math.random() < 0.1)
					drops.add(ItemStack(Material.APPLE))
			}
		}
	}
}