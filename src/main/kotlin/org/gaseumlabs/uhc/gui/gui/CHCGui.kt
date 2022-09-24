package org.gaseumlabs.uhc.gui.gui

import org.gaseumlabs.uhc.gui.*
import org.gaseumlabs.uhc.gui.guiItem.GuiItem
import org.gaseumlabs.uhc.chc.CHCType
import org.gaseumlabs.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.command.TeamCommands
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.util.Action

class CHCGui : GuiPage(
	3,
	Util.gradientString("Start game with CHC", TextColor.color(0x9e089c), TextColor.color(0x141170)),
	true
) {
	override fun createItems() = arrayOf(
		*Array(CHCType.values().size) { i ->
			val chcType = CHCType.values()[i]
			object : GuiItem(i) {
				override fun render() =
					chcType.representation()
						.name(Component.text(chcType.prettyName, Style.style(TextDecoration.BOLD)))
						.lore(chcType.description)

				override fun onClick(player: Player, shift: Boolean) {
					player.closeInventory()
					UHC.getConfig().chcType = chcType
					if (shift) {
						TeamCommands.generateRandomTeams(player, 1)
					}
					UHC.startGame { error, message -> Action.messageOrError(player, message, error) }
				}
			}
		},
		object : GuiItem(coords(8, 2)) {
			override fun onClick(player: Player, shift: Boolean) = GuiManager.openGui(player, CreateGameGui())
			override fun render() =
				ItemCreator.display(Material.PRISMARINE_SHARD).name(Component.text("Back", BLUE))
		}
	)
}
