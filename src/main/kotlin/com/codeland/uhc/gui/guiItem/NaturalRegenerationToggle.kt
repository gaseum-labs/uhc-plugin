package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class NaturalRegenerationToggle(index: Int) : GuiItemProperty <Boolean> (index, UHC.naturalRegeneration) {
	override fun onClick(player: Player, shift: Boolean) {
		UHC.naturalRegeneration.set(!UHC.naturalRegeneration.get())
	}

	override fun getStackProperty(value: Boolean): ItemStack {
		return ItemCreator.fromType(
			if (value) Material.BEEF else Material.ROTTEN_FLESH
		).lore(
			"Is natural regeneration allowed after grace?"
		).name(
			ItemCreator.enabledName("Natural Regeneration", value)
		).create()
	}
}
