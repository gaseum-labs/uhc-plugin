package com.codeland.uhc.gui

import com.codeland.uhc.event.Packet
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class MoveableGuiItem(var rawSlot: Int, val gui: MoveableGuiPage) {
	abstract fun generate(): ItemStack

	abstract fun onShiftClick(player: Player, itemStack: ItemStack)
	abstract fun onPickUp(player: Player, inventorySlot: Int)
	abstract fun canMove(player: Player, newSlot: Int, inventorySlot: Int, other: MoveableGuiItem?): Boolean

	var id = 0

	fun setName(itemStack: ItemStack, name: String) {
		val meta = itemStack.itemMeta

		meta.displayName(Component.text(Packet.intToName(id, 3)).append(Component.text(name)))

		itemStack.itemMeta = meta
	}

	fun move(player: Player, newSlot: Int, inventorySlot: Int, other: MoveableGuiItem?): Boolean {
		return if (canMove(player, newSlot, inventorySlot, other)) {
			rawSlot = newSlot
			true

		} else {
			false
		}
	}
}
