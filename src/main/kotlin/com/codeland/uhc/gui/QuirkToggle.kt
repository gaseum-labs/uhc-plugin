package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.quirk.Quirk
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class QuirkToggle(var quirk: Quirk)
    : GuiItem(true, ItemStack(quirk.representation),
    { guiItem, player ->
        guiItem as QuirkToggle

        quirk.updateEnabled(!quirk.enabled)
    }
) {
    fun setDisplayEnabled() {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.RESET}${ChatColor.WHITE}${quirk.prettyName} ${ChatColor.GRAY}- ${ChatColor.GREEN}${ChatColor.BOLD}Enabled")
        meta.addEnchant(Enchantment.CHANNELING, 1, true)
        meta.lore = quirk.description.asList()
        stack.itemMeta = meta
    }

    fun setDisplayDisabled() {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.RESET}${ChatColor.WHITE}${quirk.prettyName} ${ChatColor.GRAY}- ${ChatColor.RED}${ChatColor.BOLD}Disabled")
        meta.removeEnchant(Enchantment.CHANNELING)
        meta.lore = quirk.description.asList()
        stack.itemMeta = meta
    }

    fun updateDisplay() {
        if (quirk.enabled) {
            setDisplayEnabled()
        } else {
            setDisplayDisabled()
        }
    }
}