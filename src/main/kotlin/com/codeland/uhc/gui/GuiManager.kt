package com.codeland.uhc.gui

import com.codeland.uhc.core.PlayerData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory
import java.util.*

class GuiManager : Listener {
	companion object {
		private val guis = ArrayList<GuiPage>()

		fun <G : GuiPage> register(gui: G): G {
			val existingIndex = guis.indexOfFirst { it.type === gui.type }

			if (existingIndex == -1) {
				guis.add(gui)
			} else {
				guis[existingIndex] = gui
			}

			return gui
		}

		private fun findGui(inventory: Inventory, uuid: UUID): Pair<GuiPage?, Boolean> {
			val gui = guis.find { it.inventory === inventory }
			if (gui != null) return Pair(gui, true)

			val personalGui = PlayerData.getPlayerData(uuid).guis.find { it.inventory === inventory }
			if (personalGui != null) return Pair(personalGui, false)

			return Pair(null, false)
		}
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		val player = event.whoClicked as Player

		val (gui, needsOp) = findGui(event.inventory, player.uniqueId)
		if (gui == null) return

		gui.onClick(event, needsOp)
	}

	@EventHandler
	fun onInventoryDrag(event: InventoryDragEvent) {
		val (gui) = findGui(event.inventory, event.whoClicked.uniqueId)
		val inventory = gui?.inventory ?: return

		if (gui is MoveableGuiPage) {
			/* no dragging at all in moveable gui */
			event.isCancelled = true
		} else {
			/* do not allow dragging into the gui inventory */
			if (event.rawSlots.any { it < inventory.size }) event.isCancelled = true
		}
	}

	@EventHandler
	fun onClose(event: InventoryCloseEvent) {
		val (gui) = findGui(event.inventory, event.player.uniqueId)
		gui?.onClose(event.player as Player)
	}

	@EventHandler
	fun onPickupItem(event: EntityPickupItemEvent) {
		val player = event.entity as? Player ?: return

		if (player.openInventory.type == InventoryType.CRAFTING) return

		val (gui) = findGui(player.openInventory.topInventory, player.uniqueId)

		/* can't have external items added to moveable gui page inventory */
		if (gui is MoveableGuiPage) event.isCancelled = true
	}
}
