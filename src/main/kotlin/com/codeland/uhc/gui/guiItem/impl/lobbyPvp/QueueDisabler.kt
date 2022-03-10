package com.codeland.uhc.gui.guiItem.impl.lobbyPvp

import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.gui.guiItem.GuiItemProperty
import com.codeland.uhc.util.UHCProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QueueDisabler(index: Int, enabledProperty: UHCProperty<Boolean>) :
	GuiItemProperty<Boolean>(index, enabledProperty) {
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
				if (value) Component.text("Queue is open", GREEN)
				else Component.text("Queue is closed", GRAY)
			)
			.create()
	}
}
