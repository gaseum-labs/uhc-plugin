package com.codeland.uhc.gui

import com.codeland.uhc.core.Preset
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.Quirk
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object Gui {
    const val INVENTORY_WIDTH = 9
    const val INVENTORY_SIZE = INVENTORY_WIDTH * 4

    var inventory = Bukkit.createInventory(null, INVENTORY_SIZE, "UHC Setup")
    var guiItems = arrayOfNulls<GuiItem>(INVENTORY_SIZE)

    init {
        /* cancel button */
        addItem(GuiItem(false, ItemStack(Material.BARRIER)) { guiItem, player ->
            close(player)
        }, 8, 3).setName("${ChatColor.RESET}${ChatColor.RED}close")

        /* programmatically add all quirks */
        Quirk.values().forEach { quirk ->
            addItem(QuirkToggle(quirk), getQuirkPosition(quirk))
        }

        PhaseType.values().forEach { phaseType ->
            addItem(VariantCycler(phaseType), getPhasePosition(phaseType))
        }

        addItem(PresetCycler(), getPresetPosition())
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun close(player: Player) {
        player.closeInventory()
    }

    private fun getQuirkPosition(quirk: Quirk): Int {
        return quirk.ordinal
    }

    private fun getPhasePosition(phaseType: PhaseType): Int {
        return INVENTORY_WIDTH * 2 + phaseType.ordinal
    }

    private fun getPresetPosition(): Int {
        return INVENTORY_SIZE - INVENTORY_WIDTH
    }

    fun updateQuirk(quirk: Quirk) {
        (guiItems[getQuirkPosition(quirk)] as? QuirkToggle)?.updateDisplay()
    }

    fun updatePhaseVariant(phaseVariant: PhaseVariant) {
        (guiItems[getPhasePosition(phaseVariant.type)] as? VariantCycler)?.updateDisplay(phaseVariant)
    }

    fun updatePreset(preset: Preset) {
        (guiItems[getPresetPosition()] as? PresetCycler)?.updateDisplay(preset)
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