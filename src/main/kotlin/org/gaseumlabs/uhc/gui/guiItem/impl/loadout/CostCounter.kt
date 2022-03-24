package org.gaseumlabs.uhc.gui.guiItem.impl.loadout

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.ItemCreator.Companion.stateName
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.lobbyPvp.Loadouts
import org.gaseumlabs.uhc.util.UHCProperty
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CostCounter(index: Int, costProperty: UHCProperty<Int>) : GuiItemProperty<Int>(index, costProperty) {
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
