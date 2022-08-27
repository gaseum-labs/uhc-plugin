package org.gaseumlabs.uhc.gui.guiItem

import org.bukkit.entity.Player
import org.gaseumlabs.uhc.gui.ItemCreator

abstract class GuiItem(val index: Int) {
	abstract fun render(): ItemCreator
	abstract fun onClick(player: Player, shift: Boolean)
}
