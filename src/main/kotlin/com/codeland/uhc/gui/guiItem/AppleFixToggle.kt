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

class AppleFixToggle(gui: GuiInventory, uhc: UHC, index: Int) : GuiItem(gui, uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		uhc.appleFix = !uhc.appleFix
	}

	override fun getStack(): ItemStack {
		val stack = if (uhc.appleFix)
			setName(ItemStack(Material.APPLE), "${ChatColor.RESET}Apple Fix ${ChatColor.GRAY}- ${ChatColor.GREEN}${ChatColor.BOLD}Enabled")
		else
			setName(ItemStack(Material.OAK_SAPLING), "${ChatColor.RESET}Apple Fix ${ChatColor.GRAY}- ${ChatColor.RED}${ChatColor.BOLD}Disabled")

		return setLore(stack, listOf("Less random apple drops"))
	}
}