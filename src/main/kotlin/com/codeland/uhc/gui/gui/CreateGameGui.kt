package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.GameConfig
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiPage
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.world.WorldGenOption
import com.codeland.uhc.gui.guiItem.*
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CreateGameGui(val gameConfig: GameConfig) : GuiPage(
	3,
	Util.gradientString("Create Game", TextColor.color(0x0d5b61), TextColor.color(0x093c80))
) {
	val quirkGui = QuirkGui(gameConfig, this)
	val worldGenGui = WorldGenGui(gameConfig, this)

	init {
		addItem(NaturalRegenerationToggle(coords(0, 0)))
		addItem(KillRewardCycler(coords(1, 0)))
		addItem(DefaultEnvironmentCycler(coords(2, 0)))
		addItem(BotToggle(coords(3, 0)))

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
