package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.gui.MoveableGuiItem
import com.codeland.uhc.gui.MoveableGuiPage
import com.codeland.uhc.lobbyPvp.LoadoutItems
import com.codeland.uhc.lobbyPvp.Loadouts
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LoadoutMover(rawSlot: Int, gui: MoveableGuiPage, val playerData: PlayerData, val loadoutItem: LoadoutItems, var optionIndex: Int, val loadoutSlot: Int) : MoveableGuiItem(rawSlot, gui) {
	override fun internalGenerate(): ItemCreator {
		val creator = ItemCreator.fromStack(loadoutItem.createItem(), false)
			.lore(
				if (loadoutItem.enchantOptions.isEmpty()) ""
				else "Shift click to cycle enchants"
			)

		return creator.name("$AQUA${prettifyName(creator.type.name)} ${
			if (loadoutItem.enchantOptions.isEmpty()) {
				""
			} else {
				/* add options if there is the option */
				if (optionIndex != -1) when (val option = loadoutItem.enchantOptions[optionIndex]) {
					is LoadoutItems.Companion.EnchantOption -> {
						creator.enchant(option.enchant, option.level)
					}
					is LoadoutItems.Companion.AmountOption -> {
						creator.amount(creator.amount + option.addAmount) 
					}
				}
	
				/* different types of options have different colors */
				val color = when (loadoutItem.enchantOptions.first()) {
					is LoadoutItems.Companion.EnchantOption -> LIGHT_PURPLE
					is LoadoutItems.Companion.AmountOption -> DARK_PURPLE
					else -> RED
				}
	
				"${color}${BOLD}<${optionIndex + 1}/${loadoutItem.enchantOptions.size}> "
			}
		}${GREEN}${BOLD}$${itemCost()}")
	}

	override fun onShiftClick(player: Player, stack: ItemStack) {
		/* only change enchantments in the top inventory */
		if (rawSlot >= gui.inventory.size) return

		val enchantOptions = loadoutItem.enchantOptions
		if (enchantOptions.isEmpty()) return

		/* cycle to next option */
		++optionIndex
		if (optionIndex == enchantOptions.size) optionIndex = -1

		/* change itemstack to match new enchantments */
		generate().modify(stack)
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

	fun prettifyName(name: String): String {
		val buffer = CharArray(name.length)

		var startOfWord = true

		for (i in name.indices) {
			val c = name[i]

			buffer[i] = when {
				c == '_' -> {
					startOfWord = true
					' '
				}
				startOfWord -> {
					startOfWord = false
					c
				}
				else -> {
					c.lowercaseChar()
				}
			}
		}

		return String(buffer)
	}

	fun itemCost(): Int {
		return loadoutItem.cost + if (optionIndex == -1) 0 else loadoutItem.enchantOptions[optionIndex].addCost
	}

	override fun canMove(player: Player, newSlot: Int, inventorySlot: Int, other: MoveableGuiItem?): Boolean {
		/* always can swap in the upper inventory */
		if (newSlot < gui.inventory.size) return true

		val loadout = DataManager.loadouts.getPlayersLoadouts(player.uniqueId)[loadoutSlot]

		val cost = loadout.calculateCost() + itemCost() - if (other is LoadoutMover) other.itemCost() else 0
		if (cost > Loadouts.MAX_COST) return false

		/* update the loadout */
		loadout.ids[inventorySlot] = loadoutItem.ordinal
		loadout.options[inventorySlot] = optionIndex

		playerData.slotCosts[loadoutSlot].set(cost)

		return true
	}
}
