package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import com.codeland.uhc.gui.MoveableGuiItem
import com.codeland.uhc.gui.MoveableGuiPage
import com.codeland.uhc.gui.guiItem.CloseButton
import com.codeland.uhc.gui.guiItem.CostCounter
import com.codeland.uhc.gui.guiItem.LoadoutMover
import com.codeland.uhc.lobbyPvp.LoadoutItems
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.format.TextColor

class LoadoutGui(val playerData: PlayerData, val loadoutSlot: Int) :
MoveableGuiPage(
	4,
	Util.gradientString("Edit Loadout ${loadoutSlot + 1}", TextColor.color(0x7d0580), TextColor.color(0x910d40))
) {
	val costCounter = addItem(CostCounter(coords(7, 3), playerData.slotCosts[loadoutSlot]))
	val closeButton = addItem(CloseButton(coords(8, 3)))

	override fun createMoveableGuiItems(): ArrayList<MoveableGuiItem> {
		var list = ArrayList<MoveableGuiItem>()

		val loadout = DataManager.loadouts.getLoadouts(playerData.uuid)[loadoutSlot]

		/* keep track of which ones have been put in the inventory */
		val used = Array(LoadoutItems.values().size) { false }

		/* put the items in this loadout into the player's inventory */
		loadout.forEachIndexed { slot, id ->
			if (id != -1) {
				list.add(LoadoutMover(slot + inventory.size, this, playerData, LoadoutItems.values()[id], loadoutSlot))
				used[id] = true
			}
		}

		/* put all loadout items not in the loadout in the upper inventory */
		var addSlot = 0

		LoadoutItems.loadoutItems.forEach { loadoutItem ->
			if (!used[loadoutItem.ordinal]) {
				list.add(LoadoutMover(addSlot++, this, playerData, loadoutItem, loadoutSlot))
			}
		}

		/* init cost display */
		playerData.slotCosts[loadoutSlot].set(LoadoutItems.calculateCost(loadout))

		return list
	}

	override fun save() {
		val guild = GameRunner.bot?.guild() ?: return
		DiscordFilesystem.loadoutsFile.save(guild, DataManager.loadouts)
	}
}
