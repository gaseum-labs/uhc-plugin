package org.gaseumlabs.uhc.gui.guiItem

import org.bukkit.entity.Player
import org.gaseumlabs.uhc.util.UHCProperty
import org.gaseumlabs.uhc.util.Util

abstract class GuiItemCycler<E : Enum<E>>(index: Int, val values: Array<E>) :
	GuiItemProperty<E>(index) {
	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<E>) {
		property.set(values[
			Util.mod(property.get().ordinal + if (shift) -1 else 1, values.size)
		])
	}
}
