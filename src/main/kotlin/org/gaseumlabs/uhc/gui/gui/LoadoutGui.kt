package org.gaseumlabs.uhc.gui.gui

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.gui.MoveableGuiPage
import org.gaseumlabs.uhc.gui.guiItem.MoveableGuiItem
import org.gaseumlabs.uhc.gui.guiItem.impl.CloseButton
import org.gaseumlabs.uhc.gui.guiItem.impl.loadout.CostCounter
import org.gaseumlabs.uhc.gui.guiItem.impl.loadout.LoadoutMover
import org.gaseumlabs.uhc.lobbyPvp.LoadoutItems
import org.gaseumlabs.uhc.lobbyPvp.Loadouts
import org.gaseumlabs.uhc.util.Util
import net.kyori.adventure.text.format.TextColor
import org.gaseumlabs.uhc.gui.guiItem.GuiItem

class LoadoutGui(val playerData: PlayerData, val loadoutSlot: Int) : MoveableGuiPage(
	4,
	Util.gradientString("Edit Loadout", TextColor.color(0x7d0580), TextColor.color(0x910d40)),
	false,
) {
	override fun createItems() = arrayOf(
		CostCounter(coords(7, 3), playerData, loadoutSlot),
		CloseButton(coords(8, 3)),
	)

	override fun createMoveableGuiItems(): ArrayList<MoveableGuiItem> {
		val list = ArrayList<MoveableGuiItem>()

		val loadout = UHC.dataManager.loadouts.getPlayersLoadouts(playerData.uuid)[loadoutSlot]

		/* keep track of which ones have been put in the inventory */
		val used = Array(LoadoutItems.values().size) { false }

		/* put the items in this loadout into the player's inventory */
		loadout.ids.indices.forEach { slot ->
			val id = loadout.ids[slot]

			if (id != -1) {
				val option = loadout.options[slot]

				list.add(LoadoutMover(slot + inventory.size,
					this,
					playerData,
					LoadoutItems.values()[id],
					option,
					loadoutSlot))
				used[id] = true
			}
		}

		/* put all loadout items not in the loadout in the upper inventory */
		var addSlot = 0

		Loadouts.loadoutItems.forEach { loadoutItem ->
			if (!used[loadoutItem.ordinal]) {
				list.add(LoadoutMover(addSlot++, this, playerData, loadoutItem, -1, loadoutSlot))
			}
		}

		/* init cost display */
		playerData.getSlotCost(loadoutSlot).set(loadout.calculateCost())

		return list
	}
}
