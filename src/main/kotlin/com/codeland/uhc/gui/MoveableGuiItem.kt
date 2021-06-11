package com.codeland.uhc.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class MoveableGuiItem(var rawSlot: Int, val gui: MoveableGuiPage) {
	abstract fun generate(): ItemStack

	abstract fun onShiftClick(player: Player)
	abstract fun onPickUp(player: Player, inventorySlot: Int)
	abstract fun canMove(player: Player, newSlot: Int, inventorySlot: Int, other: MoveableGuiItem?): Boolean

	fun move(player: Player, newSlot: Int, inventorySlot: Int, other: MoveableGuiItem?): Boolean {
		return if (canMove(player, newSlot, inventorySlot, other)) {
			rawSlot = newSlot
			true

		} else {
			false
		}
	}
}
