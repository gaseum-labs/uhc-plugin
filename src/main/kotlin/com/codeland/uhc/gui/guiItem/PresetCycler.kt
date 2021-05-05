package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.Preset.Companion.NO_PRESET_REPRESENTATION
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.phase.PhaseType
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PresetCycler(index: Int) : GuiItemProperty <Preset?> (index, UHC.preset) {
	override fun onClick(player: Player, shift: Boolean) {
		val oldPreset = UHC.preset.get()

		UHC.updatePreset(Preset.values()[
			if (oldPreset == null) 0
			else (oldPreset.ordinal + 1) % Preset.values().size
		])
	}

	override fun getStackProperty(value: Preset?): ItemStack {
		return name(lore(ItemStack(value?.representation ?: NO_PRESET_REPRESENTATION), value?.createLore() ?: Preset.createLore(
			UHC.startRadius,
			UHC.endRadius,
			UHC.getTime(PhaseType.GRACE),
			UHC.getTime(PhaseType.SHRINK)
		)), stateName("Preset", value?.prettyName ?: "Custom"))
	}
}
