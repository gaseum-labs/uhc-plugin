package com.codeland.uhc.gui

import com.codeland.uhc.util.Util
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class GuiManager : Listener {
	companion object {
		private val guis = ArrayList<GuiPage>()

		fun <G : GuiPage> register(gui: G): G {
			guis.add(gui)
			return gui
		}
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
