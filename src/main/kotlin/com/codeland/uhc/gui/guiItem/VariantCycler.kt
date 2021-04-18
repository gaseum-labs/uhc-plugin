package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.phase.VariantList
import com.codeland.uhc.phase.PhaseType
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class VariantCycler(index: Int, var phaseType: PhaseType) : GuiItem(index, true) {
    override fun onClick(player: Player, shift: Boolean) {
		val variants = VariantList.list[phaseType.ordinal]
		val index = variants.indexOf(UHC.getVariant(phaseType))

		UHC.updateVariant(variants[
            (index + 1) % variants.size
		])
    }

    override fun getStack(): ItemStack {
        val variant = UHC.getVariant(phaseType)

        val stack = ItemStack(variant.representation)
        setName(stack, stateName(phaseType.prettyName, variant.prettyName))
        setLore(stack, variant.description)

        return stack
    }
}
