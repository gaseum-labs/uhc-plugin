package com.codeland.uhc.gui

import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.logging.Level

object GuiOpener {
    val OPENER_NAME = "${ChatColor.COLOR_CHAR}${ChatColor.RESET.char}${ChatColor.COLOR_CHAR}${ChatColor.AQUA.char}Open UHC Settings"
    val OPENER_MATERIAL = Material.MUSIC_DISC_WAIT

    fun createGuiOpener(): ItemStack {
        var stack = ItemStack(OPENER_MATERIAL)

        var meta = stack.itemMeta

        meta.setDisplayName(OPENER_NAME)
        meta.lore = listOf("Right click to open menu")

        stack.itemMeta = meta

        return stack
    }

    fun isGuiOpener(stack: ItemStack): Boolean {
        return stack.type === OPENER_MATERIAL && stack.itemMeta.hasLore()
    }

    fun hasGuiOpener(inventory: Inventory): Boolean {
        return inventory.contains(OPENER_MATERIAL)
    }
}