package org.gaseumlabs.uhc.gui.guiItem

import org.gaseumlabs.uhc.util.UHCProperty
import org.bukkit.inventory.ItemStack

abstract class GuiItemProperty<T>(index: Int, val property: UHCProperty<T>) : GuiItem(index) {
	final override fun getStack(): ItemStack = getStackProperty(property.get())

	abstract fun getStackProperty(value: T): ItemStack

	init {
		property.watch(::updateDisplay)
	}
}
