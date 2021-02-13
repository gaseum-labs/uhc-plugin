package com.codeland.uhc.gui.item

import org.bukkit.inventory.Inventory

enum class CommandItemType(val constructor: () -> CommandItem) {
	GUI_OPENER(::GuiOpener),
	JOIN_PVP(::JoinPvp),
	PARKOUR_CHECKPOINT(::ParkourCheckpoint),
	SPECTATE(::Spectate);

	companion object {
		val commandItemList = Array(values().size) { i ->
			values()[i].constructor()
		}

		fun getItem(type: CommandItemType): CommandItem {
			return commandItemList[type.ordinal]
		}

		fun hasItem(commandItem: CommandItem, inventory: Inventory): Boolean {
			return inventory.contents.any { stack ->
				if (stack == null) return@any false

				commandItem.isItem(stack)
			}
		}

		/**
		 * does not give the item if the item is already in inventory
		 */
		fun giveItem(commandItemType: CommandItemType, inventory: Inventory) {
			val item = getItem(commandItemType)
			if (!hasItem(item, inventory)) inventory.addItem(item.create())
		}
	}
}