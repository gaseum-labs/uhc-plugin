package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class QuirkToggle(var type: QuirkType)
    : GuiItem(true, ItemStack(type.representation),
    { guiItem, player ->
        guiItem as QuirkToggle

        val quirk = GameRunner.uhc.getQuirk(type)
        GameRunner.uhc.updateQuirk(type, !quirk.enabled)
    }
) {
    fun setDisplayEnabled() {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.RESET}${ChatColor.WHITE}${type.prettyName} ${ChatColor.GRAY}- ${ChatColor.GREEN}${ChatColor.BOLD}Enabled")
        meta.addEnchant(Enchantment.CHANNELING, 1, true)
        meta.lore = type.description.asList()
        stack.itemMeta = meta
    }

    fun setDisplayDisabled() {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.RESET}${ChatColor.WHITE}${type.prettyName} ${ChatColor.GRAY}- ${ChatColor.RED}${ChatColor.BOLD}Disabled")
        meta.removeEnchant(Enchantment.CHANNELING)
        meta.lore = type.description.asList()
        stack.itemMeta = meta
    }

    fun updateDisplay() {
        if (GameRunner.uhc.getQuirk(type).enabled) {
            setDisplayEnabled()
        } else {
            setDisplayDisabled()
        }
    }
}