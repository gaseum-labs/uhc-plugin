package com.codeland.uhc.gui

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.gui.guiItem.LoadoutSlotPicker
import com.codeland.uhc.gui.guiItem.QueueEnabler
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LobbyPvpGui(val playerData: PlayerData) : GuiPage(3, Util.gradientString("Lobby PVP", TextColor.color(0x380202), TextColor.color(0x332721))) {
	val queueEnabler = addItem(QueueEnabler(coords(4, 0), playerData.inLobbyPvpQueue))

	val slot0 = addItem(LoadoutSlotPicker(coords(3, 2), 0, playerData.loadoutSlot))
	val slot1 = addItem(LoadoutSlotPicker(coords(4, 2), 1, playerData.loadoutSlot))
	val slot2 = addItem(LoadoutSlotPicker(coords(5, 2), 2, playerData.loadoutSlot))

	private val cancelButton = addItem(object : GuiItem(coords(8, 2)) {
		override fun onClick(player: Player, shift: Boolean) = gui.close(player)
		override fun getStack() = name(ItemStack(Material.BARRIER), "${ChatColor.RED}Close")
	})
}
