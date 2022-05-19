package org.gaseumlabs.uhc.gui.gui

import org.gaseumlabs.uhc.core.GameConfig
import org.gaseumlabs.uhc.gui.*
import org.gaseumlabs.uhc.gui.guiItem.GuiItem
import org.gaseumlabs.uhc.gui.guiItem.GuiItemToggle
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuirkGui(val gameConfig: GameConfig, val createGameGui: CreateGameGui) : GuiPage(
	3,
	Util.gradientString("Quirks", TextColor.color(0x9e089c), TextColor.color(0x141170)),
	GuiType.QUIRK
) {
	init {
		QuirkType.values().forEachIndexed { i, quirkType ->
			addItem(object : GuiItemToggle(i, gameConfig.quirksEnabled[i]) {
				override fun getStackProperty(value: Boolean): ItemStack {
					return quirkType.representation()
						.name(ItemCreator.enabledName(quirkType.prettyName, value))
						.lore(quirkType.description)
						.enchant(value)
						.create()
				}
			})
		}

		addItem(object : GuiItem(coords(7, 2)) {
			override fun onClick(player: Player, shift: Boolean) = gameConfig.quirksEnabled.forEach { it.set(false) }
			override fun getStack() =
				ItemCreator.fromType(Material.MUSIC_DISC_CHIRP).name(Component.text("Reset", RED)).create()
		})
		addItem(object : GuiItem(coords(8, 2)) {
			override fun onClick(player: Player, shift: Boolean) = createGameGui.open(player)
			override fun getStack() =
				ItemCreator.fromType(Material.PRISMARINE_SHARD).name(Component.text("Back", BLUE)).create()
		})
	}
}
