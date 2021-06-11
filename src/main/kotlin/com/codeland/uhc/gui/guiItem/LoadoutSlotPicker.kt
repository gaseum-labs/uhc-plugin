package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LoadoutSlotPicker(index: Int, val slot: Int, slotProperty: UHCProperty<Int>) : GuiItemProperty<Int>(index, slotProperty) {
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

		return lore(name(
			ItemStack(
				if (value == slot)
					Material.LIME_CONCRETE
				else
					Material.RED_CONCRETE, namedSlot
			),
			enabledName("Loadout $namedSlot", value == slot)
		),
			listOf(
				Component.text("Click to select loadout $namedSlot"),
				Component.text("Shift click to edit")
			)
		)
	}
}
