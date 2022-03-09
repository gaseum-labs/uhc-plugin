package com.codeland.uhc.quirk

import com.codeland.uhc.gui.guiItem.GuiItemProperty
import com.codeland.uhc.util.UHCProperty
import org.bukkit.entity.Player

abstract class BoolToggle(index: Int, property: UHCProperty<Boolean>) : GuiItemProperty<Boolean>(index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(!property.get())
	}
}
