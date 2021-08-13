package com.codeland.uhc.gui

import com.codeland.uhc.gui.guiItem.GuiItem
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent

open class GuiPage(val height: Int, val name: Component, val type: GuiType) {
	companion object {
		const val WIDTH = 9

		fun coords(x: Int, y: Int): Int {
			return y * WIDTH + x
		}

		fun IndexToCoordinate(index: Int): Pair<Int, Int> {
			return Pair(index % WIDTH, index / WIDTH)
		}
	}

	val inventory = Bukkit.createInventory(null, WIDTH * height, name)
	val guiItems = arrayOfNulls<GuiItem>(inventory.size)

	open fun open(player: Player) {
		player.openInventory(inventory)
	}

	fun close(player: Player) {
		player.closeInventory()
	}

	open fun onClose(player: Player) {}

	fun <G : GuiItem> addItem(guiItem: G): G {
		guiItems[guiItem.index] = guiItem

		guiItem.giveGui(this)

		guiItem.updateDisplay()

		return guiItem
	}

	fun removeItem(index: Int) {
		inventory.setItem(index, null)
		guiItems[index] = null
	}

	open fun onClick(event: InventoryClickEvent, needsOp: Boolean): Boolean {
		val slot = event.rawSlot
		val player = event.whoClicked as Player

		if (
			event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
			(slot >= 0 && slot < inventory.size)
		) {
			event.isCancelled = true
		}

		if (needsOp && !player.isOp) return false
		if (slot >= inventory.size || slot < 0) return false

		val guiItem = guiItems[event.rawSlot]

		return if (guiItem != null) {
			guiItem.onClick(player, event.isShiftClick)
			true

		} else {
			false
		}
	}
}
