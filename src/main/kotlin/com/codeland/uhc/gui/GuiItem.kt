package com.codeland.uhc.gui

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class GuiItem(stack: ItemStack, onClick: (Gui, GuiItem, Player) -> Unit) {
    var stack = stack

    var onClick = onClick

    var index = 0
    var x = 0
    var y = 0

    fun setDisplayEnabled(name: String) {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.COLOR_CHAR}${ChatColor.RESET}${ChatColor.GREEN}${name} [ENABLED]")
        stack.itemMeta = meta
    }

    fun setDisplayDisabled(name: String) {
        val meta = stack.itemMeta
        meta.setDisplayName("${ChatColor.COLOR_CHAR}${ChatColor.RESET}${ChatColor.RED}${name} [DISABLED]")
        stack.itemMeta = meta
    }
}