package com.codeland.uhc.gui

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class GuiListener : Listener {
	private val guiInventories = ArrayList<GuiInventory>()

	fun registerInventory(guiInventory: GuiInventory) {
		guiInventories.add(guiInventory)
	}

	@EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
		guiInventories.any { guiInventory ->
			guiInventory.onClick(event)
		}
    }
}
