package org.gaseumlabs.uhc.gui.guiItem.impl

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.util.UHCProperty
import org.gaseumlabs.uhc.world.WorldGenOption
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class WorldGenSetting(index: Int, val option: WorldGenOption, property: UHCProperty<Boolean>) :
	GuiItemProperty<Boolean>(index, property) {
	override fun onClick(player: Player, shift: Boolean) {
		property.set(!property.get())
	}

	override fun getStackProperty(value: Boolean): ItemStack {
		return ItemCreator.fromType(option.representation)
			.name(ItemCreator.enabledName(option.prettyName, value))
			.lore(option.description)
			.enchant(value)
			.create()
	}
}
