package org.gaseumlabs.uhc.gui.guiItem.impl.lobbyPvp

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.util.UHCProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.lobbyPvp.PvpQueue

class QueueDisabler(index: Int) : GuiItemProperty<Boolean>(index) {
	override fun getProperty() = PvpQueue::enabled

	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Boolean>) {
		if (!player.isOp) return

		property.set(!property.get())
	}

	override fun renderProperty(value: Boolean) =
		ItemCreator.display(
			if (value) Material.LIME_CANDLE
			else Material.GRAY_CANDLE
		)
			.name(
				if (value) Component.text("Queue is open", GREEN)
				else Component.text("Queue is closed", GRAY)
			)
}
