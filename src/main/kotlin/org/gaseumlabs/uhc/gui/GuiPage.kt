package org.gaseumlabs.uhc.gui

import org.gaseumlabs.uhc.gui.guiItem.GuiItem
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty

abstract class GuiPage(val height: Int, val name: Component, val needsOp: Boolean) {
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

	fun update() {
		guiItems.filterNotNull().forEach { guiItem ->
			if (guiItem is GuiItemProperty<*>) guiItem.render()
		}
	}

	open fun open(player: Player) {
		player.openInventory(inventory)
	}

	fun close(player: Player) {
		player.closeInventory()
	}

	open fun onClose(player: Player) {}

	abstract fun createItems(): Array<GuiItem>

	fun addAllItems(items: Array<GuiItem>) {
		items.forEach { guiItem ->
			guiItems[guiItem.index] = guiItem
			inventory.setItem(guiItem.index, guiItem.render().create())
		}
	}

	open fun onClick(event: InventoryClickEvent): Boolean {
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
