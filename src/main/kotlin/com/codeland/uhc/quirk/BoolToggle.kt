package com.codeland.uhc.quirk

import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class BoolToggle(index: Int, property: UHCProperty<Boolean>) : GuiItemProperty <Boolean> (index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(!property.get())
	}
}
