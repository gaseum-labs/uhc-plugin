package com.codeland.uhc.gui.guiItem.impl

import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.gui.guiItem.GuiItemProperty
import com.codeland.uhc.util.UHCProperty
import com.codeland.uhc.world.WorldGenOption
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
