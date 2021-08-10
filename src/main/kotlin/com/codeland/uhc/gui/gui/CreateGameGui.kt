package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.GameConfig
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.KillReward
import com.codeland.uhc.gui.*
import com.codeland.uhc.gui.guiItem.*
import com.codeland.uhc.gui.guiItem.createGame.BotToggle
import com.codeland.uhc.gui.guiItem.createGame.DefaultEnvironmentCycler
import com.codeland.uhc.gui.guiItem.createGame.KillRewardCycler
import com.codeland.uhc.gui.guiItem.createGame.NaturalRegenerationToggle
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CreateGameGui(val gameConfig: GameConfig) : GuiPage(
	3,
	Util.gradientString("Create Game", TextColor.color(0x0d5b61), TextColor.color(0x093c80))
) {
	val quirkGui = QuirkGui(gameConfig, this)
	val worldGenGui = WorldGenGui(gameConfig, this)

	init {
		addItem(object : GuiItemToggle(coords(0, 0), gameConfig.naturalRegeneration) {
			override fun getStackProperty(value: Boolean): ItemStack {
				return ItemCreator.fromType(
					if (value) Material.BEEF else Material.ROTTEN_FLESH
				).lore(
					"Is natural regeneration allowed after grace?"
				).name(
					ItemCreator.enabledName("Natural Regeneration", value)
				).create()
			}
		})
		addItem(object : GuiItemCycler<KillReward>(coords(1, 0), gameConfig.killreward, KillReward.values()) {
			override fun getStackProperty(value: KillReward): ItemStack {
				return ItemCreator.fromType(value.representation)
					.name(ItemCreator.stateName("Kill Reward", value.prettyName))
					.lore(value.lore)
					.create()
			}
		})
		addItem(object : GuiItemCycler<World.Environment>(coords(2, 0), gameConfig.defaultWorldEnvironment, arrayOf(World.Environment.NORMAL, World.Environment.NETHER)) {
			override fun getStackProperty(value: World.Environment): ItemStack {
				return ItemCreator.fromType(
					if (value === World.Environment.NORMAL) Material.GRASS_BLOCK
					else Material.NETHERRACK
				)
					.name(ItemCreator.stateName("World",
						if (value === World.Environment.NORMAL) "Normal"
						else "Nether"
					))
					.lore("Which dimension the UHC starts in")
					.create()
			}

		})
		addItem(object : GuiItemToggle(coords(3, 0), gameConfig.usingBot) {
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
		})

		/* open quirks gui */
		addItem(object : GuiItem(coords(5, 2)) {
			override fun onClick(player: Player, shift: Boolean) = quirkGui.open(player)
			override fun getStack() = ItemCreator.fromType(Material.TOTEM_OF_UNDYING).name("${ChatColor.LIGHT_PURPLE}Quirks").create()
		})

		/* open worldgen gui */
		addItem(object : GuiItem(coords(6, 2)) {
			override fun onClick(player: Player, shift: Boolean) = worldGenGui.open(player)
			override fun getStack() = ItemCreator.fromType(Material.GOLD_ORE).name("${ChatColor.GREEN}World Gen Options").create()
		})

		/* reset button */
		addItem(object : GuiItem(coords(7, 2)) {
			override fun onClick(player: Player, shift: Boolean) = gameConfig.reset()
			override fun getStack() = ItemCreator.fromType(Material.MUSIC_DISC_CHIRP).name("${ChatColor.RED}Reset").create()
		})

		addItem(CloseButton(coords(8, 2)))
	}
}
