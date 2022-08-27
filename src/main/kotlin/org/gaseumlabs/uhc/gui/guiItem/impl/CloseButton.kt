package org.gaseumlabs.uhc.gui.guiItem.impl;

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player

class CloseButton(index: Int) : GuiItem(index) {
	override fun onClick(player: Player, shift: Boolean) {
		player.closeInventory()
	}

	override fun render() =
		ItemCreator.display(Material.BARRIER)
			.name(Component.text("Close", NamedTextColor.RED))
}
