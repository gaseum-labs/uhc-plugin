package org.gaseumlabs.uhc.gui.guiItem

import org.bukkit.entity.Player
import org.gaseumlabs.uhc.util.UHCProperty
import org.gaseumlabs.uhc.util.uhcDelegate

abstract class GuiItemToggle(index: Int) : GuiItemProperty<Boolean>(index) {
	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Boolean>) {
		if (shift) {
			property.uhcDelegate().reset()
		} else {
			property.set(!property.get())
		}
	}
}
