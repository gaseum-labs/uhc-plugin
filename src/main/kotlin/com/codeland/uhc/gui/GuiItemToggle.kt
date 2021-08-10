package com.codeland.uhc.gui

import com.codeland.uhc.util.UHCProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class GuiItemToggle(index: Int, property: UHCProperty<Boolean>) : GuiItemProperty<Boolean>(index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(if (shift) property.default else !property.get())
	}
}
