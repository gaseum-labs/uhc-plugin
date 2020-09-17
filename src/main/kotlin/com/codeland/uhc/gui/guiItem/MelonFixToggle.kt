package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiInventory
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MelonFixToggle(uhc: UHC, index: Int) : GuiItem(uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		uhc.melonFix = !uhc.melonFix
	}

	override fun getStack(): ItemStack {
		val stack = if (uhc.melonFix)
			setName(ItemStack(Material.GLISTERING_MELON_SLICE), "Melon Fix ${ChatColor.GRAY}- ${ChatColor.GREEN}${ChatColor.BOLD}Enabled")
		else
			setName(ItemStack(Material.MELON_SLICE), "Melon Fix ${ChatColor.GRAY}- ${ChatColor.RED}${ChatColor.BOLD}Disabled")

		return setLore(stack, listOf("Melons spawn in all temperate biomes", "Makes healing potions viable"))
	}
}