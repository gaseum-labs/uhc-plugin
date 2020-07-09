package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.Quirk
import com.destroystokyo.paper.utils.PaperPluginLogger
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

object Gui {
    const val INVENTORY_WIDTH = 9
    const val INVENTORY_SIZE = INVENTORY_WIDTH * 4

    var inventory = Bukkit.createInventory(null, INVENTORY_SIZE, "UHC Setup")
    var guiItems = arrayOfNulls<GuiItem>(INVENTORY_SIZE)

    init {
        /* cancel button */
        addItem(GuiItem(ItemStack(Material.BARRIER)) { guiItem, player ->
            close(player)
        }, 8, 3).setName("${ChatColor.RESET}${ChatColor.RED}close")

        /* programmatically add all quirks */
        Quirk.values().forEach { quirk ->
            addItem(QuirkToggle(quirk), getQuirkPosition(quirk))
        }

        PhaseType.values().forEach { phaseType ->
            addItem(VariantCycler(phaseType), getPhasePosition(phaseType))
        }
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun close(player: Player) {
        player.closeInventory()
    }

    fun getQuirkPosition(quirk: Quirk): Int {
        return quirk.ordinal
    }

    fun getPhasePosition(phaseType: PhaseType): Int {
        return INVENTORY_WIDTH * 2 + phaseType.ordinal
    }

    fun updateQuirk(quirk: Quirk) {
        guiItems[getQuirkPosition(quirk)]?.updateDisplay()
    }

    fun updatePhaseType(phaseType: PhaseType) {
        guiItems[getPhasePosition(phaseType)]?.updateDisplay()
    }

    fun coordinateToIndex(x: Int, y: Int): Int {
        return y * INVENTORY_WIDTH + x
    }

    fun IndexToCoordinate(index: Int): Pair<Int, Int> {
        return Pair(index % INVENTORY_WIDTH, index / INVENTORY_WIDTH)
    }

    fun addItem(item: GuiItem, x: Int, y: Int): GuiItem {
        val index = coordinateToIndex(x, y)

        return addItem(item, index)
    }

    fun addItem(item: GuiItem, index: Int): GuiItem {
        item.index = index

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
}