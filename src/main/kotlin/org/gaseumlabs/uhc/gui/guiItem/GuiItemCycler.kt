package org.gaseumlabs.uhc.gui.guiItem

import org.gaseumlabs.uhc.util.UHCProperty
import org.bukkit.entity.Player

abstract class GuiItemCycler<E : Enum<E>>(index: Int, property: UHCProperty<E>, val values: Array<E>) :
	GuiItemProperty<E>(index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(values[(property.get().ordinal + 1) % values.size])
	}
}
