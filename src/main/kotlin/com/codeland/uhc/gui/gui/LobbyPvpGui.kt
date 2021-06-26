package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.gui.GuiPage
import com.codeland.uhc.gui.guiItem.CloseButton
import com.codeland.uhc.gui.guiItem.LoadoutSlotPicker
import com.codeland.uhc.gui.guiItem.QueueDisabler
import com.codeland.uhc.gui.guiItem.QueueJoiner
import com.codeland.uhc.lobbyPvp.PvpQueue
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.format.TextColor

class LobbyPvpGui(val playerData: PlayerData) : GuiPage(3, Util.gradientString("Lobby PVP", TextColor.color(0x380202), TextColor.color(0x332721))) {
	val queueEnabler1v1 = addItem(QueueJoiner(coords(3, 0), PlayerData.PVP_QUEUE_1V1, playerData.inLobbyPvpQueue))
	val queueEnabler2V2 = addItem(QueueJoiner(coords(5, 0), PlayerData.PVP_QUEUE_2V2, playerData.inLobbyPvpQueue))

	val slot0 = addItem(LoadoutSlotPicker(coords(3, 2), 0, playerData.loadoutSlot))
	val slot1 = addItem(LoadoutSlotPicker(coords(4, 2), 1, playerData.loadoutSlot))
	val slot2 = addItem(LoadoutSlotPicker(coords(5, 2), 2, playerData.loadoutSlot))

	val queueDisabler = addItem(QueueDisabler(coords(7, 2), PvpQueue.enabled))

	val closeButton = addItem(CloseButton(coords(8, 2)))
}
