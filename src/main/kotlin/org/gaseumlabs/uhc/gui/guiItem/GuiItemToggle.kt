package org.gaseumlabs.uhc.gui.guiItem

import org.gaseumlabs.uhc.util.UHCProperty
import org.bukkit.entity.Player

abstract class GuiItemToggle(index: Int, property: UHCProperty<Boolean>) : GuiItemProperty<Boolean>(index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(if (shift) property.default else !property.get())
	}
}
