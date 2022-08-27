package org.gaseumlabs.uhc.gui.guiItem

import org.bukkit.entity.Player
import org.gaseumlabs.uhc.util.UHCProperty

abstract class GuiItemCounter(
	index: Int,
    private val low: Int,
	private val high: Int,
	private val increment: Int
) : GuiItemProperty<Int>(index) {
	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Int>) {
		property.set(
			if (shift) {
				(property.get() - increment).coerceAtLeast(low)
			} else {
				(property.get() + increment).coerceAtMost(high)
			}
		)
	}
}

abstract class GuiItemCounterF(
	index: Int,
	val low: Float,
	val high: Float,
	val increment: Float,
) : GuiItemProperty<Float>(index) {
	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Float>) {
		property.set(
			if (shift) {
				(property.get() - increment).coerceAtLeast(low)
			} else {
				(property.get() + increment).coerceAtMost(high)
			}
		)
	}
}
