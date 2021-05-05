package com.codeland.uhc.gui.item

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GuiOpener : CommandItem() {
    val MATERIAL = Material.MUSIC_DISC_WAIT

    override fun create(): ItemStack {
        val stack = ItemStack(MATERIAL)
        val meta = stack.itemMeta

        meta.displayName(Component.text("Open UHC Settings", NamedTextColor.AQUA))
        meta.lore(listOf(Component.text("Right click to open menu")))

        stack.itemMeta = meta
        return stack
    }

    override fun isItem(stack: ItemStack): Boolean {
        return stack.type === MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
    }

    override fun onUse(uhc: UHC, player: Player) {
        GuiManager.SETUP_GUI.open(player)
    }
}