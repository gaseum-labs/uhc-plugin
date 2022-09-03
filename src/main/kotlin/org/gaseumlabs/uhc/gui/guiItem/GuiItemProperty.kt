package org.gaseumlabs.uhc.gui.guiItem

import org.bukkit.entity.Player
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.util.UHCProperty
import org.gaseumlabs.uhc.util.uhcDelegate
import kotlin.reflect.jvm.isAccessible

abstract class GuiItemProperty<T>(index: Int) : GuiItem(index) {
	final override fun render(): ItemCreator = renderProperty(getProperty().getter.call())

	abstract fun getProperty(): UHCProperty<T>
	inline fun default() = getProperty().uhcDelegate().default()

	abstract fun renderProperty(value: T): ItemCreator

	final override fun onClick(player: Player, shift: Boolean) {
		onClickProperty(player, shift, getProperty())
	}

	protected abstract fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<T>)
}
