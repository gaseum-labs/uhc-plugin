package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.VariantList
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.phaseType.PhaseType
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class VariantCycler(var phaseType: PhaseType)
    : GuiItem(true, ItemStack(Material.DAMAGED_ANVIL),
    { guiItem, player ->
        guiItem as VariantCycler

        val variants = guiItem.getVariants()

        ++guiItem.currentIndex
        guiItem.currentIndex %= variants.size

        GameRunner.uhc.updateVariant(variants[guiItem.currentIndex])
    }
) {
    var currentIndex = 0

    private fun setVariantDisplay(factory: PhaseVariant) {
        changeStackType(factory.representation)

        setName("${ChatColor.RESET}${ChatColor.WHITE}${phaseType.prettyName} ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${factory.prettyName}")
        setLore(factory.description)
    }

    private fun getVariants(): ArrayList<PhaseVariant> {
        return VariantList.list[phaseType.ordinal]
    }

    fun updateDisplay(phaseVariant: PhaseVariant) {
        currentIndex = getVariants().indexOf(phaseVariant)

        if (currentIndex == -1)
            currentIndex = 0

        setVariantDisplay(getVariants()[currentIndex])
    }
}
