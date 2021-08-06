package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.phase.VariantList
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class VariantCycler(index: Int, var phaseType: PhaseType) : GuiItemProperty <PhaseVariant> (index, UHC.phaseVariants[phaseType.ordinal]) {
    override fun onClick(player: Player, shift: Boolean) {
		val variants = VariantList.list[phaseType.ordinal]
		val index = variants.indexOf(UHC.getVariant(phaseType))

		UHC.updateVariant(variants[
            (index + 1) % variants.size
		])
    }

	override fun getStackProperty(value: PhaseVariant): ItemStack {
        return ItemCreator.fromType(
	        value.representation
        ).name(
	        ItemCreator.stateName(phaseType.prettyName, value.prettyName)
        ).lore(value.description).create()
    }
}
