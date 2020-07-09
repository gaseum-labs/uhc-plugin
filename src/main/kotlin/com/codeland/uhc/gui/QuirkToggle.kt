package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.quirk.Quirk
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class QuirkToggle(quirk: Quirk)
    : GuiItem(ItemStack(quirk.representation),
    { gui, guiItem, player ->
        guiItem as QuirkToggle

        if (quirk.enabled) {
            quirk.enabled = false
            guiItem.setDisplayDisabled(quirk.name)
        } else {
            quirk.enabled = true
            guiItem.setDisplayEnabled(quirk.name)
        }
    }
) {

    var quirk = quirk

    init {
        setDisplayDisabled(quirk.name)
    }

    fun setDisplayEnabled(name: String) {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.RESET}${ChatColor.WHITE}${name} ${ChatColor.GRAY}- ${ChatColor.GREEN}${ChatColor.BOLD}ENABLED")
        meta.addEnchant(Enchantment.CHANNELING, 1, true)
        stack.itemMeta = meta
    }

    fun setDisplayDisabled(name: String) {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.RESET}${ChatColor.WHITE}${name} ${ChatColor.GRAY}- ${ChatColor.RED}${ChatColor.BOLD}DISABLED")
        meta.removeEnchant(Enchantment.CHANNELING)
        stack.itemMeta = meta
    }
}