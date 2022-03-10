package com.codeland.uhc.gui.guiItem.impl;

import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.gui.guiItem.GuiItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CloseButton(index: Int) : GuiItem(index) {
	override fun onClick(player: Player, shift: Boolean) {
		gui.close(player)
	}

	override fun getStack(): ItemStack {
		return ItemCreator.fromType(Material.BARRIER).name(Component.text("Close", NamedTextColor.RED)).create()
	}
}
