package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiInventory
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuirkToggle(uhc: UHC, index: Int, var type: QuirkType) : GuiItem(uhc, index, true) {
    override fun onClick(player: Player, shift: Boolean) {
        if (shift)
            uhc.getQuirk(type).inventory.open(player)
        else
            uhc.updateQuirk(type, !uhc.getQuirk(type).enabled)
    }

    override fun getStack(): ItemStack {
        val stack = ItemStack(type.representation)

        if (uhc.isEnabled(type)) {
            setName(stack, "${ChatColor.WHITE}${type.prettyName} ${ChatColor.GRAY}- ${ChatColor.GREEN}${ChatColor.BOLD}Enabled")
            setEnchanted(stack)
        } else {
            setName(stack, "${ChatColor.WHITE}${type.prettyName} ${ChatColor.GRAY}- ${ChatColor.RED}${ChatColor.BOLD}Disabled")
        }

        setLore(stack, type.description.asList())

        return stack
    }
}