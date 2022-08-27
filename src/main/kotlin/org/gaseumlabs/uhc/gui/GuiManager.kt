package org.gaseumlabs.uhc.gui

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import kotlin.reflect.KClass

class GuiManager : Listener {
	companion object {
		private val openGuis = HashMap<Player, GuiPage>()

		fun openGui(player: Player, guiPage: GuiPage) {
			openGuis[player]?.close(player)
			guiPage.addAllItems(guiPage.createItems())
			player.openInventory(guiPage.inventory)
			openGuis[player] = guiPage
		}

		fun <G : GuiPage>update(type: KClass<G>, typePlayer: Player?) {
			if (typePlayer == null) return
			openGuis.forEach { (player, guiPage) ->
				if (
					typePlayer == player && type.isInstance(guiPage)
				) guiPage.update()
			}
		}

		fun <G : GuiPage>update(type: KClass<G>) {
			openGuis.forEach { (_, guiPage) ->
				if (type.isInstance(guiPage)) guiPage.update()
			}
		}
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		openGuis[event.whoClicked]?.onClick(event)
	}

	@EventHandler
	fun onInventoryDrag(event: InventoryDragEvent) {
		val gui = openGuis[event.whoClicked]
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
		if (event.reason === InventoryCloseEvent.Reason.OPEN_NEW) return

		val gui = openGuis[event.player]
		if (gui != null) {
			openGuis.remove(event.player)
			gui.onClose(event.player as Player)
		}
	}

	@EventHandler
	fun onPickupItem(event: EntityPickupItemEvent) {
		val player = event.entity as? Player ?: return

		if (player.openInventory.type === InventoryType.CRAFTING) return

		val gui = openGuis[player]

		/* can't have external items added to moveable gui page inventory */
		if (gui is MoveableGuiPage) event.isCancelled = true
	}
}
