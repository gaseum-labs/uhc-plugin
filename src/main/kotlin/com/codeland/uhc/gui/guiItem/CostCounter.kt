package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.lobbyPvp.LoadoutItems
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CostCounter(index: Int, costProperty: UHCProperty<Int>): GuiItemProperty<Int>(index, costProperty) {
	override fun onClick(player: Player, shift: Boolean) {}

	override fun getStackProperty(value: Int): ItemStack {
		val remaining = LoadoutItems.MAX_COST - value

		return name(
			ItemStack(
				if (remaining == 0) Material.IRON_NUGGET else Material.GOLD_NUGGET,
				remaining.coerceAtLeast(1)
			),
			stateName("Remaining Cost", "$remaining")
		)
	}
}
