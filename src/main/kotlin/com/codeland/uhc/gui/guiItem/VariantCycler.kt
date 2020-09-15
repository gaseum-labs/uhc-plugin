package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.phase.VariantList
import com.codeland.uhc.phase.PhaseType
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class VariantCycler(uhc: UHC, index: Int, var phaseType: PhaseType) : GuiItem(uhc, index, true) {
    override fun onClick(player: Player, shift: Boolean) {
		val variants = VariantList.list[phaseType.ordinal]
		val index = variants.indexOf(uhc.getVariant(phaseType))

		uhc.updateVariant(variants[
            (index + 1) % variants.size
		])
    }

    override fun getStack(): ItemStack {
        val variant = uhc.getVariant(phaseType)

        val stack = ItemStack(variant.representation)
        setName(stack, "${ChatColor.RESET}${ChatColor.WHITE}${phaseType.prettyName} ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${variant.prettyName}")
        setLore(stack, variant.description)

        return stack
    }
}
