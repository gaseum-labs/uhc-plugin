package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class QuirkToggle(var quirkType: QuirkType)
    : GuiItem(true, ItemStack(quirkType.representation),
    { guiItem, player ->
        guiItem as QuirkToggle

        val quirk = GameRunner.uhc.getQuirk(quirkType);
        quirk.enabled = !quirk.enabled
    }
) {
    fun setDisplayEnabled() {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.RESET}${ChatColor.WHITE}${quirkType.prettyName} ${ChatColor.GRAY}- ${ChatColor.GREEN}${ChatColor.BOLD}Enabled")
        meta.addEnchant(Enchantment.CHANNELING, 1, true)
        meta.lore = quirkType.description.asList()
        stack.itemMeta = meta
    }

    fun setDisplayDisabled() {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.RESET}${ChatColor.WHITE}${quirkType.prettyName} ${ChatColor.GRAY}- ${ChatColor.RED}${ChatColor.BOLD}Disabled")
        meta.removeEnchant(Enchantment.CHANNELING)
        meta.lore = quirkType.description.asList()
        stack.itemMeta = meta
    }

    fun updateDisplay() {
        if (GameRunner.uhc.getQuirk(quirkType).enabled) {
            setDisplayEnabled()
        } else {
            setDisplayDisabled()
        }
    }
}