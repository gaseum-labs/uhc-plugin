package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiItemProperty
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
		return lore(
			name(
				ItemStack(if (value) Material.BEEF else Material.ROTTEN_FLESH),
				enabledName("Natural Regeneration", value)
			),
			listOf(Component.text("is natural regeneration allowed after grace?"))
		)
	}
}
