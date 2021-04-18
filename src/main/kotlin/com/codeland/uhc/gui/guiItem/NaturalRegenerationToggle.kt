package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class NaturalRegenerationToggle(index: Int) : GuiItem(index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		UHC.naturalRegeneration = !UHC.naturalRegeneration
	}

	override fun getStack(): ItemStack {
		val stack = ItemStack(if (UHC.naturalRegeneration) Material.BEEF else Material.ROTTEN_FLESH)

		setName(stack, enabledName("Natural Regeneration", UHC.naturalRegeneration))
		setLore(stack, listOf("is natural regeneration allowed after grace?"))

		return stack
	}
}
