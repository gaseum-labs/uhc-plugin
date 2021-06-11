package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.gui.GuiItem.Companion.lore
import com.codeland.uhc.gui.GuiItem.Companion.name
import com.codeland.uhc.gui.MoveableGuiItem
import com.codeland.uhc.gui.MoveableGuiPage
import com.codeland.uhc.lobbyPvp.LoadoutItems
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LoadoutMover(rawSlot: Int, gui: MoveableGuiPage, val playerData: PlayerData, val loadoutItem: LoadoutItems, val loadoutSlot: Int) : MoveableGuiItem(rawSlot, gui) {
	override fun generate(): ItemStack {
		val stack = loadoutItem.createItem()

		stack.hashCode()
		name(stack, "$AQUA$BOLD${stack.type.name} ${GRAY}- $GREEN$${loadoutItem.cost}")

		if (loadoutItem.enchantOptions.isNotEmpty())
			lore(stack, listOf(Component.text("Shift click to cycle enchants (Coming soon)")))

		return stack
	}

	override fun onShiftClick(player: Player) {
		//TODO change enchantments
	}

	override fun onPickUp(player: Player, inventorySlot: Int) {
		/* only matters if you pick up from bottom inventory */
		if (rawSlot >= gui.inventory.size) {
			val loadout = DataManager.loadouts.getLoadouts(player.uniqueId)[loadoutSlot]
			loadout[inventorySlot] = -1

			val cost = LoadoutItems.calculateCost(loadout)
			playerData.slotCosts[loadoutSlot].set(cost)
		}
	}

	override fun canMove(player: Player, newSlot: Int, inventorySlot: Int): Boolean {
		return if (newSlot >= gui.inventory.size) {
			val loadout = DataManager.loadouts.getLoadouts(player.uniqueId)[loadoutSlot]
			val cost = LoadoutItems.calculateCost(loadout) + loadoutItem.cost

			return if (cost > LoadoutItems.MAX_COST) {
				false

			} else {
				/* update the loadout */
				loadout[inventorySlot] = loadoutItem.ordinal
				playerData.slotCosts[loadoutSlot].set(cost)

				true
			}
		} else {
			true
		}
	}
}
