package com.codeland.uhc.gui.guiItem.impl.loadout

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.gui.guiItem.GuiItemProperty
import com.codeland.uhc.util.UHCProperty
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LoadoutSlotPicker(index: Int, val slot: Int, slotProperty: UHCProperty<Int>) :
	GuiItemProperty<Int>(index, slotProperty) {
	override fun onClick(player: Player, shift: Boolean) {
		/* edit */
		if (shift) {
			PlayerData.getPlayerData(player.uniqueId).slotGuis[slot].open(player)
			/* select */
		} else {
			property.set(slot)
		}
	}

	override fun getStackProperty(value: Int): ItemStack {
		val namedSlot = slot + 1

		return ItemCreator.fromType(
			if (value == slot) Material.LIME_CONCRETE
			else Material.RED_CONCRETE
		).lore(
			"Click to select loadout $namedSlot",
			"Shift click to edit"
		).name(
			ItemCreator.enabledName("Loadout $namedSlot", value == slot)
		).amount(namedSlot).create()
	}
}
