package org.gaseumlabs.uhc.gui.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.command.TeamCommands
import org.gaseumlabs.uhc.core.KillReward
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.gui.GuiPage
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.*
import org.gaseumlabs.uhc.gui.guiItem.impl.CloseButton
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.Util

class CreateGameGui : GuiPage(
	3,
	Util.gradientString("Create Game", TextColor.color(0x0d5b61), TextColor.color(0x093c80)),
	true
) {
	override fun createItems(): Array<GuiItem> = arrayOf(
		object : GuiItemToggle(coords(0, 0)) {
			override fun getProperty() = UHC.getConfig()::naturalRegeneration
			override fun renderProperty(value: Boolean) =
				ItemCreator.display(
					if (value) Material.BEEF else Material.ROTTEN_FLESH
				).lore(
					Component.text("Is natural regeneration allowed after grace?")
				).name(
					ItemCreator.enabledName("Natural Regeneration", value)
				)
		},

		object : GuiItemToggle(
			coords(3, 0),
		) {
			override fun getProperty() = UHC.getConfig()::usingBot
			override fun renderProperty(value: Boolean) =
				ItemCreator.display(
					if (UHC.bot == null) Material.GUNPOWDER
					else if (value) Material.NAUTILUS_SHELL
					else Material.HONEYCOMB
				).lore(Component.text("Separate teams into separate discord vcs?")).name(
					if (UHC.bot == null) Component.text("Bot is not running", NamedTextColor.RED, TextDecoration.BOLD)
					else ItemCreator.enabledName("Bot VCs", value)
				)
		},

		object : GuiItem(coords(3, 2)) {
			override fun onClick(player: Player, shift: Boolean) {
				player.closeInventory()
				if (shift) {
					TeamCommands.generateRandomTeams(player, 1)
				}
				UHC.startGame { error, message -> Action.messageOrError(player, message, error) }
			}
			override fun render() =
				ItemCreator.display(Material.ENCHANTING_TABLE)
					.name(Component.text("Start UHC", NamedTextColor.GOLD))
		},

		object : GuiItem(coords(5, 2)) {
			override fun onClick(player: Player, shift: Boolean) = GuiManager.openGui(player, CHCGui())
			override fun render() =
				ItemCreator.display(Material.BREWING_STAND)
					.name(Component.text("Start CHC", NamedTextColor.GOLD))
		},

		object : GuiItem(coords(7, 2)) {
			override fun onClick(player: Player, shift: Boolean) = UHC.getConfig().reset()
			override fun render() =
				ItemCreator.display(Material.MUSIC_DISC_CHIRP).name(Component.text("Reset", NamedTextColor.RED))
		},
		CloseButton(coords(8, 2))
	)
}
