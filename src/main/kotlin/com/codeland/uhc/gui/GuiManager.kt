package com.codeland.uhc.gui

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class GuiManager : Listener {
	companion object {
		val SETUP_GUI = SetupGui()

		val guis = arrayListOf <GuiPage> (
			SETUP_GUI
		)
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		/* which gui is being clicked */
		val gui = guis.find { it.inventory === event.inventory } ?: return

		val inventory = gui.inventory
		val slot = event.rawSlot
		val player = event.whoClicked as Player

		event.isCancelled = true

		if (slot >= inventory.size || slot < 0) return
		if (!player.isOp) return

		gui.onClick(player, gui.guiItems[slot], event.isShiftClick, slot)
	}
}
