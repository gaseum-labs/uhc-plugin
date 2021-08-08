package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuirkToggle(
	index: Int,
	var type: QuirkType,
	property: UHCProperty<Boolean>
) : GuiItemProperty <Boolean> (index, property) {
    override fun onClick(player: Player, shift: Boolean) {
        if (shift) UHC.getQuirk(type).gui.open(player)
        else UHC.getQuirk(type).toggleEnabled()
    }

    override fun getStackProperty(value: Boolean): ItemStack {
        return UHC.getQuirk(type).representation
	        .name(ItemCreator.enabledName(type.prettyName, value))
	        .lore(type.description)
	        .enchant(UHC.isEnabled(type))
	        .create()
    }
}