package org.gaseumlabs.uhc.gui.gui

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.gui.GuiPage
import org.gaseumlabs.uhc.gui.guiItem.impl.CloseButton
import org.gaseumlabs.uhc.gui.guiItem.impl.loadout.LoadoutSlotPicker
import org.gaseumlabs.uhc.gui.guiItem.impl.lobbyPvp.*
import org.gaseumlabs.uhc.lobbyPvp.PvpQueue
import org.gaseumlabs.uhc.util.Util
import net.kyori.adventure.text.format.TextColor
import org.gaseumlabs.uhc.gui.guiItem.GuiItem

class QueueGUI(val playerData: PlayerData) : GuiPage(
	3,
	Util.gradientString("Lobby PVP", TextColor.color(0x380202), TextColor.color(0x332721)),
	false
) {
	override fun createItems() = arrayOf(
		QueueJoiner(coords(3, 0), PvpQueue.TYPE_1V1, playerData),
		QueueJoiner(coords(4, 0), PvpQueue.TYPE_GAP, playerData),
		QueueJoiner(coords(5, 0), PvpQueue.TYPE_2V2, playerData),
		LoadoutSlotPicker(coords(3, 2), 0, playerData),
		LoadoutSlotPicker(coords(4, 2), 1, playerData),
		LoadoutSlotPicker(coords(5, 2), 2, playerData),
		SpectatePvp(coords(0, 2)),
		QueueDisabler(coords(7, 2)),
		ParkourJoiner(coords(8, 0), playerData),
		ParkourCreator(coords(7, 0)),
		CloseButton(coords(8, 2)),
	)
}
