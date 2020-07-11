package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.Factories
import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.logging.Level

class VariantCycler(var phaseType: PhaseType)
    : GuiItem(true, ItemStack(Material.DAMAGED_ANVIL),
    { guiItem, player ->
        guiItem as VariantCycler

        val factories = guiItem.getFactories()

        ++guiItem.currentIndex
        guiItem.currentIndex %= factories.size

        guiItem.phaseType.factory = factories[guiItem.currentIndex]
    }
) {
    var currentIndex = 0

    private fun setFactoryDisplay(factory: PhaseFactory) {
        changeStackType(factory.representation)

        setName("${ChatColor.RESET}${ChatColor.WHITE}${phaseType.prettyName} ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${factory.prettyName}")
    }

    private fun getFactories(): ArrayList<PhaseFactory> {
        return Factories.list[phaseType.ordinal]
    }

    override fun updateDisplay() {
        currentIndex = getFactories().indexOf(phaseType.factory)

        if (currentIndex == -1)
            currentIndex = 0

        setFactoryDisplay(getFactories()[currentIndex])
    }
}