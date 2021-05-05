package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuirkToggle(index: Int, var type: QuirkType) : GuiItemProperty <Boolean> (index, UHC.getQuirk(type).enabled) {
    override fun onClick(player: Player, shift: Boolean) {
        if (shift) UHC.getQuirk(type).gui.open(player)

        else UHC.updateQuirk(type, !UHC.getQuirk(type).enabled.get())
    }

    override fun getStackProperty(value: Boolean): ItemStack {
        val stack = lore(
	        name(UHC.getQuirk(type).representation, enabledName(type.prettyName, UHC.isEnabled(type))),
	        type.description
        )

        return if (UHC.isEnabled(type)) enchant(stack) else stack
    }
}