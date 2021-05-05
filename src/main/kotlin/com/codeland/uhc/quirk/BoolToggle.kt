package com.codeland.uhc.quirk

import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BoolToggle(index: Int, property: UHCProperty<Boolean>, val onTrue: () -> ItemStack, val onFalse: () -> ItemStack) : GuiItemProperty <Boolean> (index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(!property.get())
	}

	override fun getStackProperty(value: Boolean): ItemStack {
		return if (value) onTrue() else onFalse()
	}
}