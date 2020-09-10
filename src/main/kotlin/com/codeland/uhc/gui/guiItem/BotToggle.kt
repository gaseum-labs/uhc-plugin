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

class BotToggle(uhc: UHC, index: Int) : GuiItem(uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		uhc.updateUsingBot(!uhc.usingBot)
	}

	override fun getStack(): ItemStack {
		val stack: ItemStack

		if (GameRunner.bot == null) {
			stack = ItemStack(Material.GUNPOWDER)
			setName(stack, "${ChatColor.RED}${ChatColor.BOLD}Bot is not running")
		} else {
			if (uhc.usingBot) {
				stack = ItemStack(Material.NAUTILUS_SHELL)
				setName(stack, "${ChatColor.WHITE}Bot Vcs ${ChatColor.GRAY}- ${ChatColor.GREEN}${ChatColor.BOLD}Enabled")
			} else {
				stack = ItemStack(Material.HONEYCOMB)
				setName(stack, "${ChatColor.WHITE}Bot Vcs ${ChatColor.GRAY}- ${ChatColor.RED}${ChatColor.BOLD}Disabled")
			}
		}

		setLore(stack, listOf("Separate teams into separate discord vcs?"))

		return stack
	}
}