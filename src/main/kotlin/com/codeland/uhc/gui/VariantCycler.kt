package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.Factories
import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class VariantCycler(phaseType: PhaseType)
    : GuiItem(ItemStack(Material.DAMAGED_ANVIL),
    { gui, guiItem, player ->
        guiItem as VariantCycler

        val factories = guiItem.getFactories()

        ++guiItem.currentIndex
        guiItem.currentIndex %= factories.size

        val factory = factories[guiItem.currentIndex]

        GameRunner.uhc.phaseFactories[guiItem.phaseType.ordinal] = factory
        guiItem.setFactoryDisplay(factory)
    }
) {
    var phaseType = phaseType
    var currentIndex = 0

    private fun setFactoryDisplay(factory: PhaseFactory) {
        changeStackType(factory.representation)

        setName("${ChatColor.RESET}${ChatColor.WHITE}${phaseType.prettyName} ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${factory.prettyName}")
    }

    private fun getFactories(): ArrayList<PhaseFactory> {
        return Factories.list[phaseType.ordinal]
    }

    init {
        setFactoryDisplay(getFactories()[currentIndex])
    }
}