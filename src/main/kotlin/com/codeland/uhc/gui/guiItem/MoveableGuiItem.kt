package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.gui.MoveableGuiPage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class MoveableGuiItem(var rawSlot: Int, val gui: MoveableGuiPage) {
	var id = 0

	abstract fun internalGenerate(): ItemCreator

	abstract fun onShiftClick(player: Player, itemStack: ItemStack)
	abstract fun onPickUp(player: Player, inventorySlot: Int)
	abstract fun canMove(player: Player, newSlot: Int, inventorySlot: Int, other: MoveableGuiItem?): Boolean

	fun generate() = internalGenerate().setData(MoveableGuiPage.key, id)

	fun move(player: Player, newSlot: Int, inventorySlot: Int, other: MoveableGuiItem?): Boolean {
		return if (canMove(player, newSlot, inventorySlot, other)) {
			rawSlot = newSlot
			true

		} else {
			false
		}
	}
}
