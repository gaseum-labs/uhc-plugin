package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.Factories
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.phaseType.PhaseType
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class VariantCycler(var phaseType: PhaseType)
    : GuiItem(true, ItemStack(Material.DAMAGED_ANVIL),
    { guiItem, player ->
        guiItem as VariantCycler

        val factories = guiItem.getFactories()

        ++guiItem.currentIndex
        guiItem.currentIndex %= factories.size

        GameRunner.uhc.setVariant(factories[guiItem.currentIndex])
    }
) {
    var currentIndex = 0

    private fun setFactoryDisplay(factory: PhaseVariant) {
        changeStackType(factory.representation)

        setName("${ChatColor.RESET}${ChatColor.WHITE}${phaseType.prettyName} ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${factory.prettyName}")
    }

    private fun getFactories(): ArrayList<PhaseVariant> {
        return Factories.list[phaseType.ordinal]
    }

    fun updateDisplay(phaseFactory: PhaseVariant) {
        currentIndex = getFactories().indexOf(phaseFactory)

        if (currentIndex == -1)
            currentIndex = 0

        setFactoryDisplay(getFactories()[currentIndex])
    }
}
