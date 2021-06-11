package com.codeland.uhc.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class MoveableGuiItem(var slot: Int, val gui: MoveableGuiPage) {
	abstract fun generate(): ItemStack

	abstract fun onShiftClick(player: Player)

	abstract fun onPickUp(player: Player)

	fun onMove(player: Player, newSlot: Int): Boolean {
		return if (canMove(player, newSlot)) {
			slot = newSlot
			true

		} else {
			false
		}
	}

	abstract fun canMove(player: Player, newSlot: Int): Boolean
}
