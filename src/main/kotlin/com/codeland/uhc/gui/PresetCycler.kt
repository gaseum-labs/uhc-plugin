package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Preset
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class PresetCycler : GuiItem(true, ItemStack(Material.SKULL_BANNER_PATTERN),
	{ guiItem, plasyer ->
		guiItem as PresetCycler

		++guiItem.currentIndex
		guiItem.currentIndex %= Preset.values().size

		GameRunner.uhc.updatePreset(Preset.values()[guiItem.currentIndex])
	}
) {
	var currentIndex = 0

	fun updateDisplay(preset: Preset) {
		changeStackType(preset.representation)

		setName("${ ChatColor.RESET}${ ChatColor.WHITE}Preset ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${preset.prettyName}")
		setLore(preset.createLore())
	}
}
