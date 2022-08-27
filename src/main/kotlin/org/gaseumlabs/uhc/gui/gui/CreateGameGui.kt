package org.gaseumlabs.uhc.gui.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
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
			override fun property() = UHC.getConfig()::naturalRegeneration
			override fun renderProperty(value: Boolean) =
				ItemCreator.display(
					if (value) Material.BEEF else Material.ROTTEN_FLESH
				).lore(
					Component.text("Is natural regeneration allowed after grace?")
				).name(
					ItemCreator.enabledName("Natural Regeneration", value)
				)
		},
		object : GuiItemCycler<KillReward>(
			coords(1, 0),
			arrayOf(KillReward.APPLE, KillReward.ABSORPTION, KillReward.REGENERATION, KillReward.NONE)
		) {
			override fun property() = UHC.getConfig()::killReward
			override fun renderProperty(value: KillReward) =
				ItemCreator.display(value.representation)
					.name(ItemCreator.stateName("Kill Reward", value.prettyName))
					.lore(value.lore)
		},
		object : GuiItemCycler<World.Environment>(
			coords(2, 0),
			arrayOf(World.Environment.NORMAL, World.Environment.NETHER)
		) {
			override fun property() = UHC.getConfig()::defaultWorldEnvironment
			override fun renderProperty(value: World.Environment) =
				ItemCreator.display(
					if (value === World.Environment.NORMAL) Material.GRASS_BLOCK
					else Material.NETHERRACK
				)
					.name(ItemCreator.stateName("World",
						if (value === World.Environment.NORMAL) "Normal"
						else "Nether"
					))
					.lore(Component.text("Which dimension the UHC starts in"))
		},
		object : GuiItemToggle(
			coords(3, 0),
		) {
			override fun property() = UHC.getConfig()::usingBot
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

		object : GuiItemCounterF(coords(0, 1), 0.5f, 6.0f, 0.1f) {
			override fun property() = UHC.getConfig()::scale
			override fun renderProperty(value: Float) =
				ItemCreator.display(Material.WARPED_TRAPDOOR)
					.enchant(value != default())
					.name(ItemCreator.stateName("Scale", "%.2f".format(value)))
		},
		object : GuiItemCounter(coords(1, 1), 10, 200, 5) {
			override fun property() = UHC.getConfig()::battlegroundRadius
			override fun renderProperty(value: Int) =
				ItemCreator.display(Material.WARPED_DOOR)
					.enchant(value != default())
					.name(ItemCreator.stateName("Battleground Radius", "$value"))
		},
		object : GuiItemCounter(coords(2, 1), 60, 6000, 60) {
			override fun property() = UHC.getConfig()::graceTime
			override fun renderProperty(value: Int) =
				ItemCreator.display(Material.SUNFLOWER)
					.enchant(value != default())
					.name(ItemCreator.stateName("Grace Time", Util.timeString(value)))
		},
		object : GuiItemCounter(coords(3, 1), 60, 6000, 60) {
			override fun property() = UHC.getConfig()::shrinkTime
			override fun renderProperty(value: Int) =
				ItemCreator.display(Material.BELL)
					.enchant(value != default())
					.name(ItemCreator.stateName("Shrink Time", Util.timeString(value)))
		},
		object : GuiItemCounter(coords(4, 1), 60, 6000, 60) {
			override fun property() = UHC.getConfig()::battlegroundTime
			override fun renderProperty(value: Int) =
				ItemCreator.display(Material.CAMPFIRE)
					.enchant(value != default())
					.name(ItemCreator.stateName("Battleground Time", Util.timeString(value)))
		},
		object : GuiItemCounter(coords(5, 1), 60, 1000, 30) {
			override fun property() = UHC.getConfig()::collapseTime
			override fun renderProperty(value: Int) =
				ItemCreator.display(Material.HORN_CORAL)
					.enchant(value != default())
					.name(ItemCreator.stateName("Collapse Time", Util.timeString(value)))
		},

		object : GuiItem(coords(4, 2)) {
			override fun onClick(player: Player, shift: Boolean) {
				player.closeInventory()
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
