package com.codeland.uhc.gui

import com.codeland.uhc.event.Packet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

abstract class MoveableGuiPage(height: Int, name: Component): GuiPage(height, name) {
	var moveableGuiItems = ArrayList<MoveableGuiItem>()
	val savedIventory = arrayOfNulls<ItemStack>(36)

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

		moveableGuiItems.forEachIndexed { id, item ->
			val stack = giveItemId(item.generate(), id)

			if (item.rawSlot < inventory.size) {
				inventory.setItem(item.rawSlot, stack)
			} else {
				player.inventory.setItem(item.rawSlot - inventory.size, stack)
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

	fun giveItemId(itemStack: ItemStack, id: Int): ItemStack {
		val meta = itemStack.itemMeta

		val oldDisplayName = meta.displayName() ?: Component.empty()
		meta.displayName(Component.text(Packet.intToName(id, 3)).append(oldDisplayName))

		itemStack.itemMeta = meta

		return itemStack
	}

	fun getItem(itemStack: ItemStack?): MoveableGuiItem? {
		if (itemStack?.type == Material.AIR) return null

		val name = (itemStack?.itemMeta?.displayName() as? TextComponent)?.content() ?: return null

		val id = Packet.nameToInt(name, 3) ?: return null

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
				clickedItem.onShiftClick(player)

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
