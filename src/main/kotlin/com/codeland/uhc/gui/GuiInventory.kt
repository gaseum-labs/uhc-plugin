package com.codeland.uhc.gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class GuiInventory(val height: Int, val name: String) {
	companion object {
		const val WIDTH = 9
	}

	val inventory = Bukkit.createInventory(null, WIDTH * height, name)
	val guiItems = arrayOfNulls<GuiItem>(inventory.size)

	fun open(player: Player) {
		player.openInventory(inventory)
	}

	fun close(player: Player) {
		player.closeInventory()
	}

	fun coordinateToIndex(x: Int, y: Int): Int {
		return y * WIDTH + x
	}

	fun IndexToCoordinate(index: Int): Pair<Int, Int> {
		return Pair(index % WIDTH, index / WIDTH)
	}

	fun <ItemType : GuiItem> addItem(guiItem: ItemType): ItemType {
		val index = guiItem.index

		inventory.setItem(index, guiItem.getStack())
		guiItems[index] = guiItem

		val guiStack = inventory.getItem(index) ?: return guiItem
		guiItem.guiStack = guiStack

		return guiItem
	}

	fun removeItem(index: Int) {
		inventory.setItem(index, null)
		guiItems[index] = null
	}

	fun onClick(event: InventoryClickEvent): Boolean {
		if (event.inventory !== inventory) return false

		val slot = event.rawSlot
		if (slot >= inventory.size || slot < 0) return false

		event.isCancelled = true

		val guiItem = guiItems[slot] ?: return false

		/* only ops may modify */
		val player = event.whoClicked
		if (guiItem.opOnly && !player.isOp) return false

		/* do guiItem action */
		guiItem.onClick(player as Player, event.isShiftClick)

		/* then change its item to reflect the action */
		guiItem.updateDisplay()

		return true
	}
}
