package com.codeland.uhc.gui.item

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class GuiOpener : CommandItem() {
    val MATERIAL = Material.MUSIC_DISC_WAIT

    override fun create(): ItemStack {
        var stack = ItemStack(MATERIAL)
        var meta = stack.itemMeta

        meta.setDisplayName("${ChatColor.RESET}${ChatColor.AQUA}Open UHC Settings")
        meta.lore = listOf("Right click to open menu")

        stack.itemMeta = meta
        return stack
    }

    override fun isItem(stack: ItemStack): Boolean {
        return stack.type === MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
    }

    override fun onUse(uhc: UHC, player: Player) {
        uhc.gui.inventory.open(player)
    }
}