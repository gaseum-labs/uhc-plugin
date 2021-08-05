package com.codeland.uhc.gui.item

import com.codeland.uhc.core.UHC
import com.codeland.uhc.event.Chat
import com.codeland.uhc.gui.GuiManager
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.naming.Name

class ParkourTest : CommandItem() {
    val MATERIAL = Material.RABBIT_FOOT

    override fun create(): ItemStack {
        val stack = ItemStack(MATERIAL)
        val meta = stack.itemMeta

        meta.displayName(Component.text("${ChatColor.GOLD}Test Parkour"))
        meta.lore(listOf(Component.text("Right click to test")))

        stack.itemMeta = meta
        return stack
    }

    override fun isItem(stack: ItemStack): Boolean {
        return stack.type === MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
    }

    override fun onUse(uhc: UHC, player: Player) {
        val arena = ArenaManager.playersArena(player.uniqueId) as? ParkourArena ?: return

        arena.enterPlayer(player, player.gameMode === GameMode.CREATIVE, false)
    }
}
