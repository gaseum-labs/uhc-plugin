package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BotToggle(index: Int) : GuiItemProperty <Boolean> (index, UHC.usingBot) {
	override fun onClick(player: Player, shift: Boolean) {
		UHC.usingBot.set(!UHC.usingBot.get())
	}

	override fun getStackProperty(value: Boolean): ItemStack {
		return ItemCreator.fromType(
			if (GameRunner.bot == null) Material.GUNPOWDER
			else if (value) Material.NAUTILUS_SHELL
			else Material.HONEYCOMB
		).lore("Separate teams into separate discord vcs?").name(
			if (GameRunner.bot == null) Component.text("Bot is not running", NamedTextColor.RED, TextDecoration.BOLD)
			else ItemCreator.enabledName("Bot VCs", value)
		).create()
	}
}