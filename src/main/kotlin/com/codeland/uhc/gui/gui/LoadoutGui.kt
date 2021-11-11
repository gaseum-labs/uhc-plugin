package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.DataManager
import com.codeland.uhc.discord.database.file.LoadoutsFile
import com.codeland.uhc.gui.GuiType
import com.codeland.uhc.gui.MoveableGuiPage
import com.codeland.uhc.gui.guiItem.MoveableGuiItem
import com.codeland.uhc.gui.guiItem.impl.CloseButton
import com.codeland.uhc.gui.guiItem.impl.loadout.CostCounter
import com.codeland.uhc.gui.guiItem.impl.loadout.LoadoutMover
import com.codeland.uhc.lobbyPvp.LoadoutItems
import com.codeland.uhc.lobbyPvp.Loadouts
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.format.TextColor

class LoadoutGui(val playerData: PlayerData, val loadoutSlot: Int) :
MoveableGuiPage(
	4,
	Util.gradientString("Edit Loadout", TextColor.color(0x7d0580), TextColor.color(0x910d40)),
	GuiType.PERSONAL
) {
	val costCounter = addItem(CostCounter(coords(7, 3), playerData.slotCosts[loadoutSlot]))
	val closeButton = addItem(CloseButton(coords(8, 3)))

	override fun createMoveableGuiItems(): ArrayList<MoveableGuiItem> {
		var list = ArrayList<MoveableGuiItem>()

		val loadout = UHC.dataManager.loadouts.getPlayersLoadouts(playerData.uuid)[loadoutSlot]

		/* keep track of which ones have been put in the inventory */
		val used = Array(LoadoutItems.values().size) { false }

		/* put the items in this loadout into the player's inventory */
		loadout.ids.indices.forEach { slot ->
			val id = loadout.ids[slot]

			if (id != -1) {
				val option = loadout.options[slot]

				list.add(LoadoutMover(slot + inventory.size, this, playerData, LoadoutItems.values()[id], option, loadoutSlot))
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
		playerData.slotCosts[loadoutSlot].set(loadout.calculateCost())

		return list
	}

	companion object {
		var globalSaveCount = 0
	}

	override fun save() {
		val connection = UHC.dataManager.connection
		if (connection != null) {
			DataManager.loadoutsFile.push(
				connection,
				LoadoutsFile.LoadoutEntry(playerData.uuid, loadoutSlot, UHC.dataManager.loadouts.getPlayersLoadouts(playerData.uuid)[loadoutSlot])
			)
		}
	}
}
