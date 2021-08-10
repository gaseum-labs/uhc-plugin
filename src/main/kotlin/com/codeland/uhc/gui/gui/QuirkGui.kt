package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.GameConfig
import com.codeland.uhc.gui.guiItem.GuiItem
import com.codeland.uhc.gui.guiItem.GuiItemToggle
import com.codeland.uhc.gui.GuiPage
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuirkGui(val gameConfig: GameConfig, val createGameGui: CreateGameGui) : GuiPage(
	3,
	Util.gradientString("Quirks", TextColor.color(0x9e089c), TextColor.color(0x141170))
) {
	init {
		QuirkType.values().forEachIndexed { i, quirkType ->
			addItem(object : GuiItemToggle(i, gameConfig.quirksEnabled[i]) {
				override fun getStackProperty(value: Boolean): ItemStack {
					return quirkType.representation()
						.name(ItemCreator.enabledName(quirkType.prettyName, value))
						.lore(*quirkType.description)
						.enchant(value)
						.create()
				}
			})
		}

		addItem(object : GuiItem(coords(7, 2)) {
			override fun onClick(player: Player, shift: Boolean) = gameConfig.quirksEnabled.forEach { it.set(false) }
			override fun getStack() = ItemCreator.fromType(Material.MUSIC_DISC_CHIRP).name("${ChatColor.RED}Reset").create()
		})
		addItem(object : GuiItem(coords(8, 2)) {
			override fun onClick(player: Player, shift: Boolean) = createGameGui.open(player)
			override fun getStack() = ItemCreator.fromType(Material.PRISMARINE).name("${ChatColor.BLUE}Back").create()
		})
	}
}
