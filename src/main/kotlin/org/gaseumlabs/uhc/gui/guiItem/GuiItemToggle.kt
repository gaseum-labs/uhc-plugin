package org.gaseumlabs.uhc.gui.guiItem

import org.bukkit.entity.Player
import org.gaseumlabs.uhc.util.UHCProperty

abstract class GuiItemToggle(index: Int) : GuiItemProperty<Boolean>(index) {
	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Boolean>) {
		if (shift) {
			property.getDelegate().reset()
		} else {
			property.set(!property.get())
		}
	}
}
