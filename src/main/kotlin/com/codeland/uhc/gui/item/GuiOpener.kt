package com.codeland.uhc.gui.item

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object GuiOpener {
    val MATERIAL = Material.MUSIC_DISC_WAIT

    fun create(): ItemStack {
        var stack = ItemStack(MATERIAL)
        var meta = stack.itemMeta

        meta.setDisplayName("${ChatColor.RESET}${ChatColor.AQUA}Open UHC Settings")
        meta.lore = listOf("Right click to open menu")

        stack.itemMeta = meta
        return stack
    }

    fun isItem(stack: ItemStack): Boolean {
        return stack.type === MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
    }

    fun hasItem(inventory: Inventory): Boolean {
        return inventory.contents.any { stack ->
            if (stack == null) return@any false

            isItem(stack)
        }
    }
}