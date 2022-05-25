package org.gaseumlabs.uhc.gui.gui

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.gui.GuiPage
import org.gaseumlabs.uhc.gui.GuiType
import org.gaseumlabs.uhc.gui.guiItem.impl.CloseButton
import org.gaseumlabs.uhc.gui.guiItem.impl.loadout.LoadoutSlotPicker
import org.gaseumlabs.uhc.gui.guiItem.impl.lobbyPvp.*
import org.gaseumlabs.uhc.lobbyPvp.PvpQueue
import org.gaseumlabs.uhc.lobbyPvp.arena.PvpArena
import org.gaseumlabs.uhc.util.Util
import net.kyori.adventure.text.format.TextColor
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena

class LobbyPvpGui(val playerData: PlayerData) : GuiPage(3,
	Util.gradientString("Lobby PVP", TextColor.color(0x380202), TextColor.color(0x332721)),
	GuiType.PERSONAL) {
	val queueEnabler1V1 = addItem(QueueJoiner(coords(3, 0), PvpQueue.TYPE_1V1, playerData.inLobbyPvpQueue))
	val queueEnablerGap = addItem(QueueJoiner(coords(4, 0), PvpQueue.TYPE_GAP, playerData.inLobbyPvpQueue))
	val queueEnabler2V2 = addItem(QueueJoiner(coords(5, 0), PvpQueue.TYPE_2V2, playerData.inLobbyPvpQueue))

	val slot0 = addItem(LoadoutSlotPicker(coords(3, 2), 0, playerData.loadoutSlot))
	val slot1 = addItem(LoadoutSlotPicker(coords(4, 2), 1, playerData.loadoutSlot))
	val slot2 = addItem(LoadoutSlotPicker(coords(5, 2), 2, playerData.loadoutSlot))

	val spectatePvp = addItem(SpectatePvp(coords(0, 2)))
	val queueDisabler = addItem(QueueDisabler(coords(7, 2), PvpQueue.enabled))

	val parkourJoiner = addItem(ParkourJoiner(coords(8, 0), playerData.parkourIndex))
	val parkourCreator = addItem(ParkourCreator(coords(7, 0)))

	val closeButton = addItem(CloseButton(coords(8, 2)))
}
