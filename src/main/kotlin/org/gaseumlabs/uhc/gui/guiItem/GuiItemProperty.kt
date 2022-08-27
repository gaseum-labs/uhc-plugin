package org.gaseumlabs.uhc.gui.guiItem

import org.bukkit.entity.Player
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.util.UHCProperty
import kotlin.reflect.KMutableProperty0

abstract class GuiItemProperty<T>(index: Int) : GuiItem(index) {
	final override fun render(): ItemCreator = renderProperty(property().getter.call())

	abstract fun property(): KMutableProperty0<T>
	fun prop() = property() as UHCProperty<T>
	fun default() = prop().getDelegate().default()

	abstract fun renderProperty(value: T): ItemCreator

	final override fun onClick(player: Player, shift: Boolean) {
		onClickProperty(player, shift, property() as UHCProperty<T>)
	}

	protected abstract fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<T>)
}
