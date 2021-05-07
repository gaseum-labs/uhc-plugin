package com.codeland.uhc.gui

import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.WorldGenOption
import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.gui.guiItem.*
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SetupGui : GuiPage(5, Util.gradientString("UHC Setup", TextColor.color(0x34ebb4), TextColor.color(0x093c80))) {

	private val quirkToggles = Array(QuirkType.values().size) { i -> QuirkToggle(i, QuirkType.values()[i]) }

	private val variantCyclers = Array(PhaseType.values().size) { i -> VariantCycler(i + (WIDTH * 3), PhaseType.values()[i]) }

	private val presetCycler = PresetCycler(coords(0, 4))
	private val killRewardCycler = KillRewardCycler(coords(1, 4))
	private val botToggle = BotToggle(coords(2, 4))
	private val defaultEnvironmentCycler = DefaultEnvironmentCycler(coords(3, 4))
	private val naturalRegenerationToggle = NaturalRegenerationToggle(coords(4, 4))

	private val worldButton = object : GuiItem(coords(6, 4)) {
		override fun onClick(player: Player, shift: Boolean) = WorldGenOption.worldGenGui.open(player)
		override fun getStack() = name(ItemStack(Material.BARRIER), "${ChatColor.GREEN}World Gen Options")
	}

	private val resetButton = object : GuiItem(coords(7, 4)) {
		override fun onClick(player: Player, shift: Boolean) {
			UHC.defaultVariants.forEach { variant ->
				UHC.updateVariant(variant)
			}

			QuirkType.values().forEach { quirkType ->
				val quirk = UHC.getQuirk(quirkType)

				quirk.enabled.reset()
				quirk.resetProperties()
			}

			UHC.properties.forEach { it.reset() }
		}

		override fun getStack() = name(ItemStack(Material.MUSIC_DISC_WAIT), "${ChatColor.AQUA}Reset")
	}

	private val cancelButton: GuiItem = object : GuiItem(coords(8, 4)) {
		override fun onClick(player: Player, shift: Boolean) = gui.close(player)
		override fun getStack() = name(ItemStack(Material.BARRIER), "${ChatColor.RED}Close")
	}

	init {
		quirkToggles.forEach { addItem(it) }
		variantCyclers.forEach { addItem(it) }

		addItem(presetCycler)
		addItem(killRewardCycler)
		addItem(botToggle)
		addItem(defaultEnvironmentCycler)
		addItem(naturalRegenerationToggle)
		addItem(resetButton)
		addItem(cancelButton)
	}
}
