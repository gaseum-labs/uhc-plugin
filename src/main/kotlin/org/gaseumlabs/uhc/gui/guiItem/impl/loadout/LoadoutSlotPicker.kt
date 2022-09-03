package org.gaseumlabs.uhc.gui.guiItem.impl.loadout

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.util.UHCProperty
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.gui.gui.LoadoutGui

class LoadoutSlotPicker(
	index: Int,
	val loadoutSlot: Int,
	val playerData: PlayerData
) : GuiItemProperty<Int>(index) {
	override fun getProperty() = playerData::loadoutSlot

	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Int>) {
		/* edit */
		if (shift) {
			GuiManager.openGui(player, LoadoutGui(playerData, loadoutSlot))
		} else {
			property.set(loadoutSlot)
		}
	}

	val namedSlot = loadoutSlot + 1

	override fun renderProperty(value: Int) =
		ItemCreator.display(
			if (value == loadoutSlot) Material.LIME_CONCRETE
			else Material.RED_CONCRETE
		).lore(listOf(
			Component.text("Click to select loadout $namedSlot"),
			Component.text("Shift click to edit")
		)).name(
			ItemCreator.enabledName("Loadout $namedSlot", value == loadoutSlot)
		).amount(namedSlot)
}
