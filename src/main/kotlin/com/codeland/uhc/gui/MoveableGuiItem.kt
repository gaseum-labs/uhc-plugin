package com.codeland.uhc.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class MoveableGuiItem(var rawSlot: Int, val gui: MoveableGuiPage) {
	abstract fun generate(): ItemStack

	abstract fun onShiftClick(player: Player)

	abstract fun onPickUp(player: Player, inventorySlot: Int)

	fun onMove(player: Player, newSlot: Int, inventorySlot: Int): Boolean {
		return if (canMove(player, newSlot, inventorySlot)) {
			rawSlot = newSlot
			true

		} else {
			false
		}
	}

	abstract fun canMove(player: Player, newSlot: Int, inventorySlot: Int): Boolean
}
