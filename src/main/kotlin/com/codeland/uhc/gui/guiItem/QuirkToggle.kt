package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiInventory
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuirkToggle(index: Int, var type: QuirkType) : GuiItem(index, true) {
    override fun onClick(player: Player, shift: Boolean) {
        if (shift) UHC.getQuirk(type).inventory.open(player)
        else UHC.updateQuirk(type, !UHC.getQuirk(type).enabled)
    }

    override fun getStack(): ItemStack {
        val stack = setName(UHC.getQuirk(type).representation, enabledName(type.prettyName, UHC.isEnabled(type)))
        if (UHC.isEnabled(type)) setEnchanted(stack)

        setLore(stack, type.description.asList())

        return stack
    }
}