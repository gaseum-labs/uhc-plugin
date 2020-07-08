package com.codeland.uhc.gui

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class Gui : Listener {
    val INVENTORY_SIZE = 27
    val INVENTORY_WIDTH = 9

    var inventory = Bukkit.createInventory(null, INVENTORY_SIZE, "UHC Setup")
    var guiItems = arrayOfNulls<GuiItem>(INVENTORY_SIZE)

    constructor() {
        /* cancel button */
        addItem(GuiItem(ItemStack(Material.BARRIER)) { player ->
            close(player)
        }, 8, 2)

        /* demo button */
        addItem(GuiItem(ItemStack(Material.LAPIS_LAZULI)) { player ->
            val message = TextComponent("hi")
            message.color = ChatColor.GOLD
            message.isBold = true

            player.sendMessage(message)
        }, 4, 1)
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun close(player: Player) {
        player.closeInventory()
    }

    fun coordinateToIndex(x: Int, y: Int): Int {
        return y * INVENTORY_WIDTH + x
    }

    fun IndexToCoordinate(index: Int): Pair<Int, Int> {
        return Pair(index % INVENTORY_WIDTH, index / INVENTORY_WIDTH)
    }

    fun addItem(item: GuiItem, x: Int, y: Int) {
        val index = coordinateToIndex(x, y)

        item.index = index
        item.x = x
        item.y = y

        inventory.setItem(index, item.stack)
        guiItems[index] = item
    }

    fun removeItem(x: Int, y: Int) {
        val index = coordinateToIndex(x, y)
        removeItem(index)
    }

    fun removeItem(index: Int) {
        inventory.setItem(index, null)
        guiItems[index] = null
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        /* only work on this inventory */
        if (event.inventory !== inventory)
            return

        event.isCancelled = true

        val slot = event.rawSlot
        val guiItem = guiItems[slot];
        val player = event.whoClicked

        /* only ops may modify */
        if (!player.isOp)
            return

        if (guiItem == null)
            return

        /* do guiItem action */
        guiItem.onClick(player as Player)
    }
}