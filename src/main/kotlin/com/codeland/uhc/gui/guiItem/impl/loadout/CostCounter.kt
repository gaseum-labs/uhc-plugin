package com.codeland.uhc.gui.guiItem.impl.loadout

import com.codeland.uhc.util.UHCProperty
import com.codeland.uhc.gui.guiItem.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.gui.ItemCreator.Companion.stateName
import com.codeland.uhc.lobbyPvp.Loadouts
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CostCounter(index: Int, costProperty: UHCProperty<Int>): GuiItemProperty<Int>(index, costProperty) {
	override fun onClick(player: Player, shift: Boolean) {}

	override fun getStackProperty(value: Int): ItemStack {
		val remaining = Loadouts.MAX_COST - value

		return ItemCreator.fromType(
			if (remaining == 0) Material.IRON_NUGGET
			else Material.GOLD_NUGGET
		)
		.amount(remaining.coerceAtLeast(1))
		.name(stateName("Remaining Cost", "$remaining"))
		.create()
	}
}
