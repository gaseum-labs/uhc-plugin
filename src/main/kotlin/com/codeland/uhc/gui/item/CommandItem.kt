package com.codeland.uhc.gui.item

import com.codeland.uhc.core.UHC
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class CommandItem {
	abstract fun create(): ItemStack
	abstract fun isItem(stack: ItemStack): Boolean
	abstract fun onUse(uhc: UHC, player: Player)
}