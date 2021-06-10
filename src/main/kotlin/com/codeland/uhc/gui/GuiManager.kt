package com.codeland.uhc.gui

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GuiManager : Listener {
	companion object {
		private val guis = ArrayList<GuiPage>()
		private val personalGuis = HashMap<UUID, ArrayList<GuiPage>>()

		fun <G : GuiPage> register(gui: G): G {
			guis.add(gui)
			return gui
		}

		fun <G : GuiPage> registerPersonal(uuid: UUID, gui: G): G {
			personalGuis.getOrPut(uuid) { ArrayList() }.add(gui)
			return gui
		}

		private fun findGui(inventory: Inventory, uuid: UUID): Pair<GuiPage?, Boolean> {
			val gui = guis.find { it.inventory === inventory }
			if (gui != null) return Pair(gui, true)

			val personalGui = personalGuis[uuid]?.find { it.inventory === inventory }
			if (personalGui != null) return Pair(personalGui, false)

			return Pair(null, false)
		}
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		val player = event.whoClicked as Player
		val slot = event.rawSlot

		val (gui, needsOp) = findGui(event.inventory, player.uniqueId)
		if (gui == null) return

		/* do not manipulate the gui inventory */
		if (
			event.action == MOVE_TO_OTHER_INVENTORY ||
			(slot >= 0 && slot < gui.inventory.size)
		) {
			event.isCancelled = true
		}

		if (slot >= gui.inventory.size || slot < 0) return
		if (needsOp && !player.isOp) return

		gui.onClick(player, gui.guiItems[slot], event.isShiftClick, slot)
	}

	@EventHandler
	fun onInventoryDrag(event: InventoryDragEvent) {
		val (gui) = findGui(event.inventory, event.whoClicked.uniqueId)
		val inventory = gui?.inventory ?: return

		/* do not allow dragging into the gui inventory */
		if (event.rawSlots.any { it < inventory.size }) event.isCancelled = true
	}
}
