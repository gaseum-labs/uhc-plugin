package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.Preset.Companion.NO_PRESET_REPRESENTATION
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.phase.PhaseType
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PresetCycler(index: Int) : GuiItem(index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		val oldPreset = UHC.preset

		UHC.updatePreset(Preset.values()[
			if (oldPreset == null) 0
			else (oldPreset.ordinal + 1) % Preset.values().size
		])
	}

	override fun getStack(): ItemStack {
		val preset = UHC.preset
		val stack = ItemStack(preset?.representation ?: NO_PRESET_REPRESENTATION)

		setLore(stack, preset?.createLore() ?: Preset.createLore(
			UHC.startRadius,
			UHC.endRadius,
			UHC.getTime(PhaseType.GRACE),
			UHC.getTime(PhaseType.SHRINK)
		))

		setName(stack, "${ChatColor.WHITE}Preset ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${preset?.prettyName ?: "Custom"}")

		return stack
	}
}
