package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.gui.GuiItem.Companion.lore
import com.codeland.uhc.gui.MoveableGuiItem
import com.codeland.uhc.gui.MoveableGuiPage
import com.codeland.uhc.lobbyPvp.LoadoutItems
import com.codeland.uhc.lobbyPvp.Loadouts
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LoadoutMover(rawSlot: Int, gui: MoveableGuiPage, val playerData: PlayerData, val loadoutItem: LoadoutItems, var option: Int, val loadoutSlot: Int) : MoveableGuiItem(rawSlot, gui) {
	override fun generate(): ItemStack {
		val stack = loadoutItem.createItem()

		stack.hashCode()

		if (loadoutItem.enchantOptions.isNotEmpty())
			lore(stack, listOf(Component.text("Shift click to cycle enchants")))

		updateItem(stack)

		return stack
	}

	override fun onShiftClick(player: Player, itemStack: ItemStack) {
		/* only change enchantments in the top inventory */
		if (rawSlot >= gui.inventory.size) return

		val enchantOptions = loadoutItem.enchantOptions
		if (enchantOptions.isEmpty()) return

		/* cycle to next option */
		++option
		if (option == enchantOptions.size) option = -1

		/* change itemstack to match new enchantments */
		updateItem(itemStack)
	}

	override fun onPickUp(player: Player, inventorySlot: Int) {
		/* only matters if you pick up from bottom inventory */
		if (rawSlot >= gui.inventory.size) {
			val loadout = DataManager.loadouts.getPlayersLoadouts(player.uniqueId)[loadoutSlot]
			loadout.ids[inventorySlot] = -1
			loadout.options[inventorySlot] = -1

			playerData.slotCosts[loadoutSlot].set(loadout.calculateCost())
		}
	}

	fun updateItem(itemStack: ItemStack) {
		val enchantOptions = loadoutItem.enchantOptions

		/* only set enchantments on an enchant option item*/
		if (enchantOptions.isNotEmpty()) {
			val meta = itemStack.itemMeta

			meta.enchants.forEach { (enchant, _) -> meta.removeEnchant(enchant) }

			/* add enchant if there is the option */
			if (option != -1) {
				meta.addEnchant(enchantOptions[option].enchant, enchantOptions[option].level, true)
			}

			itemStack.itemMeta = meta
		}

		setName(itemStack, "$AQUA$BOLD${itemStack.type.name} ${GRAY}- $GREEN$${itemCost()}")
	}

	fun itemCost(): Int {
		return loadoutItem.cost + if (option == -1) 0 else loadoutItem.enchantOptions[option].addCost
	}

	override fun canMove(player: Player, newSlot: Int, inventorySlot: Int, other: MoveableGuiItem?): Boolean {
		/* always can swap in the upper inventory */
		if (newSlot < gui.inventory.size) return true

		val loadout = DataManager.loadouts.getPlayersLoadouts(player.uniqueId)[loadoutSlot]

		val cost = loadout.calculateCost() + itemCost() - if (other is LoadoutMover) other.itemCost() else 0
		if (cost > Loadouts.MAX_COST) return false

		/* update the loadout */
		loadout.ids[inventorySlot] = loadoutItem.ordinal
		loadout.options[inventorySlot] = option

		playerData.slotCosts[loadoutSlot].set(cost)

		return true
	}
}
