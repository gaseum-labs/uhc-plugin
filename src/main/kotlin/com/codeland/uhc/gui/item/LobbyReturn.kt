package com.codeland.uhc.gui.item

import com.codeland.uhc.core.UHC
import com.codeland.uhc.event.Chat
import com.codeland.uhc.gui.GuiManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LobbyReturn : CommandItem() {
    val MATERIAL = Material.MAGMA_CREAM
    val NAME = "${ChatColor.WHITE}Use ${ChatColor.BOLD}/uhc lobby ${ChatColor.WHITE}to return to lobby"

    override fun create(): ItemStack {
        val stack = ItemStack(MATERIAL)
        val meta = stack.itemMeta

        meta.displayName(Component.text(NAME))
        meta.lore(listOf(Component.text(NAME)))

        stack.itemMeta = meta
        return stack
    }

    override fun isItem(stack: ItemStack): Boolean {
        return stack.type === MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
    }

    override fun onUse(uhc: UHC, player: Player) {
	    player.performCommand("uhc lobby")
    }
}