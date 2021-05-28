package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.Preset
import com.codeland.uhc.core.Setup
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItemProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PresetCycler(index: Int) : GuiItemProperty <Setup> (index, UHC.setup) {
	override fun onClick(player: Player, shift: Boolean) {
		val oldPresetIndex = (Preset.findPreset(UHC.setup.get()) ?: Preset.values().last()).ordinal

		UHC.updateSetup { Preset.values()[(oldPresetIndex + 1) % Preset.values().size].setup }
	}

	override fun getStackProperty(value: Setup): ItemStack {
		return name(lore(ItemStack(value.representation), value.createLore()), stateName("Preset", value.prettyName))
	}
}
