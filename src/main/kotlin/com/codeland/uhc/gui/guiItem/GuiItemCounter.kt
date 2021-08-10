package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.util.UHCProperty
import org.bukkit.entity.Player

abstract class GuiItemCounter(index: Int, property: UHCProperty<Int>, val low: Int, val high: Int, val increment: Int) : GuiItemProperty<Int>(index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(
			if (shift) {
				(property.get() - increment).coerceAtLeast(low)
			} else {
				(property.get() + increment).coerceAtMost(high)
			}
		)
	}
}

abstract class GuiItemCounterF(index: Int, property: UHCProperty<Float>, val low: Float, val high: Float, val increment: Float) : GuiItemProperty<Float>(index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(
			if (shift) {
				(property.get() - increment).coerceAtLeast(low)
			} else {
				(property.get() + increment).coerceAtMost(high)
			}
		)
	}
}
