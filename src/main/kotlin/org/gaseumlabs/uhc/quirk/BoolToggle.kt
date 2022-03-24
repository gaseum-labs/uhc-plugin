package org.gaseumlabs.uhc.quirk

import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.util.UHCProperty
import org.bukkit.entity.Player

abstract class BoolToggle(index: Int, property: UHCProperty<Boolean>) : GuiItemProperty<Boolean>(index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(!property.get())
	}
}
