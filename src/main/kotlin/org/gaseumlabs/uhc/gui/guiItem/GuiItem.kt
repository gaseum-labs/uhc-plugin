package org.gaseumlabs.uhc.gui.guiItem

import org.gaseumlabs.uhc.gui.GuiPage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class GuiItem(val index: Int) {
	protected lateinit var gui: GuiPage

	fun giveGui(gui: GuiPage) {
		this.gui = gui
	}

	abstract fun getStack(): ItemStack
	abstract fun onClick(player: Player, shift: Boolean)

	fun updateDisplay() {
		gui.inventory.setItem(index, getStack())
	}
}
