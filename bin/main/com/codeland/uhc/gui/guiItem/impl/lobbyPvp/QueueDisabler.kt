package com.codeland.uhc.gui.guiItem.impl.lobbyPvp

import com.codeland.uhc.util.UHCProperty
import com.codeland.uhc.gui.guiItem.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QueueDisabler(index: Int, enabledProperty: UHCProperty<Boolean>) : GuiItemProperty<Boolean>(index, enabledProperty) {
	override fun onClick(player: Player, shift: Boolean) {
		if (!player.isOp) return

		property.set(!property.get())
	}

	override fun getStackProperty(value: Boolean): ItemStack {
		return ItemCreator.fromType(
			if (value) Material.LIME_CANDLE
			else Material.GRAY_CANDLE
		)
		.name(
			if (value) "${ChatColor.GREEN}Queue is open"
			else "${ChatColor.GRAY}Queue is closed"
		)
		.create()
	}
}
