package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.ItemStack
import java.util.logging.Level

class Gui : Listener {
    val INVENTORY_WIDTH = 9
    val INVENTORY_SIZE = INVENTORY_WIDTH * 3

    var inventory = Bukkit.createInventory(null, INVENTORY_SIZE, "UHC Setup")
    var guiItems = arrayOfNulls<GuiItem>(INVENTORY_SIZE)

    constructor() {
        /* cancel button */
        addItem(GuiItem(ItemStack(Material.BARRIER)) { gui, guiItem, player ->
            close(player)
        }, 8, 2).setName("${ChatColor.RESET}${ChatColor.RED}close")

        /* toggles */
        addItem(QuirkToggle(ItemStack(Material.IRON_SWORD), GameRunner.halfZatoichi), 0, 0)
        addItem(QuirkToggle(ItemStack(Material.BLUE_ORCHID), GameRunner.abundance), 1, 0)
        addItem(QuirkToggle(ItemStack(Material.SHULKER_SHELL), GameRunner.unsheltered), 2, 0)
        addItem(QuirkToggle(ItemStack(Material.LEATHER_CHESTPLATE), GameRunner.pests), 3, 0)
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

    fun addItem(item: GuiItem, x: Int, y: Int): GuiItem {
        val index = coordinateToIndex(x, y)

        item.index = index
        item.x = x
        item.y = y

        inventory.setItem(index, item.stack)
        guiItems[index] = item

        item.updateStack(inventory.getItem(index)!!)

        return item
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

        /* make sure it is in top inventory not player's */
        val slot = event.rawSlot
        if (slot >= INVENTORY_SIZE || slot < 0)
            return

        val guiItem = guiItems[slot]
                ?: return

        /* only ops may modify */
        val player = event.whoClicked
        if (!player.isOp)
            return

        /* do guiItem action */
        guiItem.onClick(this, guiItem, player as Player)
    }
}