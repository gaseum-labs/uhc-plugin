package com.codeland.uhc.gui.guiItem;

import com.codeland.uhc.gui.GuiItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CloseButton(index: Int): GuiItem(index) {
	override fun onClick(player: Player, shift: Boolean) {
		gui.close(player)
	}

	override fun getStack(): ItemStack {
		return name(ItemStack(Material.BARRIER), "${ChatColor.RED}Close")
	}
}
