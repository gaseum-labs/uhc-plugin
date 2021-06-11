package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.gui.GuiPage
import com.codeland.uhc.gui.guiItem.CloseButton
import com.codeland.uhc.gui.guiItem.LoadoutSlotPicker
import com.codeland.uhc.gui.guiItem.QueueEnabler
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.format.TextColor

class LobbyPvpGui(val playerData: PlayerData) : GuiPage(3, Util.gradientString("Lobby PVP", TextColor.color(0x380202), TextColor.color(0x332721))) {
	val queueEnabler = addItem(QueueEnabler(coords(4, 0), playerData.inLobbyPvpQueue))

	val slot0 = addItem(LoadoutSlotPicker(coords(3, 2), 0, playerData.loadoutSlot))
	val slot1 = addItem(LoadoutSlotPicker(coords(4, 2), 1, playerData.loadoutSlot))
	val slot2 = addItem(LoadoutSlotPicker(coords(5, 2), 2, playerData.loadoutSlot))

	private val closeButton = addItem(CloseButton(coords(8, 2)))
}
