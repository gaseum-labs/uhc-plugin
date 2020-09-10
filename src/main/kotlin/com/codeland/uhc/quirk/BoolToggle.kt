package com.codeland.uhc.quirk

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiInventory
import com.codeland.uhc.gui.GuiItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BoolToggle(uhc: UHC, index: Int, val property: BoolProperty, val onTrue: () -> ItemStack, val onFalse: () -> ItemStack) : GuiItem(uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		property.value = !property.value
	}

	override fun getStack(): ItemStack {
		return if (property.value)
			onTrue()
		else
			onFalse()
	}
}