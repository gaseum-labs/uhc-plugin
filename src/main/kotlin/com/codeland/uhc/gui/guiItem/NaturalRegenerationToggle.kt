package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class NaturalRegenerationToggle(uhc: UHC, index: Int) : GuiItem(uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		uhc.naturalRegeneration = !uhc.naturalRegeneration
	}

	override fun getStack(): ItemStack {
		val stack = ItemStack(if (uhc.naturalRegeneration) Material.BEEF else Material.ROTTEN_FLESH)

		setName(stack, enabledName("Natural Regeneration", uhc.naturalRegeneration))
		setLore(stack, listOf("is natural regeneration allowed after grace?"))

		return stack
	}
}
