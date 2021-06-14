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

class LoadoutMover(rawSlot: Int, gui: MoveableGuiPage, val playerData: PlayerData, val loadoutItem: LoadoutItems, var optionIndex: Int, val loadoutSlot: Int) : MoveableGuiItem(rawSlot, gui) {
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
		++optionIndex
		if (optionIndex == enchantOptions.size) optionIndex = -1

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
					c.toLowerCase()
				}
			}
		}

		return String(buffer)
	}

	fun updateItem(itemStack: ItemStack) {
		val options = loadoutItem.enchantOptions

		/* only set enchantments on an enchant option item*/
		val cyclePart = if (options.isEmpty()) {
			""

		} else {
			/* get a fresh itemMeta */
			val baseItem = loadoutItem.createItem()
			val meta = baseItem.itemMeta

			itemStack.amount = baseItem.amount

			/* add options if there is the option */
			if (optionIndex != -1) {
				val option = options[optionIndex]

				when (option) {
					is LoadoutItems.Companion.EnchantOption -> {
						meta.addEnchant(option.enchant, option.level, true)
					}
					is LoadoutItems.Companion.AmountOption -> {
						itemStack.amount += option.addAmount
					}
				}
			}

			itemStack.itemMeta = meta

			/* different types of options have different colors */
			val color = when (options.first()) {
				is LoadoutItems.Companion.EnchantOption -> LIGHT_PURPLE
				is LoadoutItems.Companion.AmountOption -> DARK_PURPLE
				else -> RED
			}

			"${color}${BOLD}<${optionIndex + 1}/${options.size}> "
		}

		setName(itemStack, "$AQUA${prettifyName(itemStack.type.name)} ${cyclePart}${GREEN}${BOLD}$${itemCost()}")
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
