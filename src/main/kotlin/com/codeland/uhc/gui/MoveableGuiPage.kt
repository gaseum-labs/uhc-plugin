package com.codeland.uhc.gui

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.gui.guiItem.MoveableGuiItem
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

abstract class MoveableGuiPage(height: Int, name: Component): GuiPage(height, name) {
	var moveableGuiItems = ArrayList<MoveableGuiItem>()
	val savedIventory = arrayOfNulls<ItemStack>(36)

	companion object {
		val key = NamespacedKey(UHCPlugin.plugin, "gid")
	}

	abstract fun createMoveableGuiItems(): ArrayList<MoveableGuiItem>
	abstract fun save()

	override fun open(player: Player) {
		super.open(player)

		/* save player inventory */
		for (i in 0..35) {
			savedIventory[i] = player.inventory.getItem(i)
			player.inventory.setItem(i, null)
		}

		/* generate the moveable gui items */
		moveableGuiItems = createMoveableGuiItems()

		moveableGuiItems.forEachIndexed { index, moveable ->
			moveable.id = index
			val creator = moveable.generate().setData(key, index)

			if (moveable.rawSlot < inventory.size) {
				inventory.setItem(moveable.rawSlot, creator.create())
			} else {
				player.inventory.setItem(moveable.rawSlot - inventory.size, creator.create())
			}
		}
	}

	override fun onClose(player: Player) {
		save()

		/* restore player's inventory */
		for (i in 0..35) {
			player.inventory.setItem(i, savedIventory[i])
		}

		/* clear moveable gui items from the gui inventory */
		for (i in 0 until inventory.size) {
			if (guiItems[i] == null) inventory.setItem(i, null)
		}
	}

	fun getItem(stack: ItemStack?): MoveableGuiItem? {
		if (stack == null || stack.type === Material.AIR) return null

		val id = ItemCreator.getData(key, stack) ?: return null

		return moveableGuiItems[id]
	}

	override fun onClick(event: InventoryClickEvent, needsOp: Boolean): Boolean {
		if (!super.onClick(event, needsOp)) {
			event.isCancelled = true

			val rawSlot = event.rawSlot
			val player = event.whoClicked as Player

			if (needsOp && !player.isOp) return false

			val clickedItem = getItem(event.currentItem)
			val heldItem = getItem(player.itemOnCursor)

			/* shift clicking does not move */
			if (clickedItem != null && event.action == MOVE_TO_OTHER_INVENTORY) {
				clickedItem.onShiftClick(player, event.currentItem!!)

			/* placing item from the cursor */
			} else if (heldItem != null && event.action == PLACE_ALL) {
				if (heldItem.move(player, rawSlot, event.slot, null)) event.isCancelled = false

			/* placing an item, also picking one up */
			} else if (heldItem != null && clickedItem != null && event.action == SWAP_WITH_CURSOR) {
				if (heldItem.move(player, rawSlot, event.slot, clickedItem)) event.isCancelled = false

			/* putting an item onto cursor */
			} else if (clickedItem != null && event.action == PICKUP_ALL) {
				clickedItem.onPickUp(player, event.slot)
				event.isCancelled = false
			}
		}

		return true
	}
}
