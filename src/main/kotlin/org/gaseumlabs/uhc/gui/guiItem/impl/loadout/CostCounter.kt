package org.gaseumlabs.uhc.gui.guiItem.impl.loadout

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.ItemCreator.Companion.stateName
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.lobbyPvp.Loadouts
import org.gaseumlabs.uhc.util.UHCProperty
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.core.PlayerData

class CostCounter(index: Int, val playerData: PlayerData, val slot: Int) : GuiItemProperty<Int>(index) {
	override fun property() = playerData.getSlotCost(slot)

	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Int>) {}

	override fun renderProperty(value: Int): ItemCreator {
		val remaining = Loadouts.MAX_COST - value

		return ItemCreator.display(
			if (remaining == 0) Material.IRON_NUGGET
			else Material.GOLD_NUGGET
		)
			.amount(remaining.coerceAtLeast(1))
			.name(stateName("Remaining Cost", "$remaining"))
	}
}
