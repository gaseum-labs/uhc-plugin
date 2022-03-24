package org.gaseumlabs.uhc.gui.guiItem.impl.loadout

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.util.UHCProperty
import net.kyori.adventure.text.Component
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
		).lore(listOf(
			Component.text("Click to select loadout $namedSlot"),
			Component.text("Shift click to edit")
		)).name(
			ItemCreator.enabledName("Loadout $namedSlot", value == slot)
		).amount(namedSlot).create()
	}
}
