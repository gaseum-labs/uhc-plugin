package com.codeland.uhc.gui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player

open class GuiPage(val height: Int, val name: Component) {
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

	fun coords(x: Int, y: Int): Int {
		return y * WIDTH + x
	}

	fun IndexToCoordinate(index: Int): Pair<Int, Int> {
		return Pair(index % WIDTH, index / WIDTH)
	}

	fun addItem(guiItem: GuiItem) {
		guiItems[guiItem.index] = guiItem

		guiItem.giveGui(this)

		guiItem.updateDisplay()
	}

	fun removeItem(index: Int) {
		inventory.setItem(index, null)
		guiItems[index] = null
	}

	open fun onClick(player: Player, guiItem: GuiItem?, shift: Boolean, slot: Int) {
		guiItem?.onClick(player, shift)
	}
}
