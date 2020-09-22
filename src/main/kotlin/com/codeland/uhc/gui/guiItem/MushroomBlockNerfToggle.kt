package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MushroomBlockNerfToggle(uhc: UHC, index: Int) : GuiItem(uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		uhc.mushroomBlockNerf = !uhc.mushroomBlockNerf
	}

	override fun getStack(): ItemStack {
		val stack = setName(ItemStack(if (uhc.mushroomBlockNerf) Material.SUSPICIOUS_STEW else Material.BOWL), enabledName("Mushroom Block Nerf", uhc.mushroomBlockNerf))

		return setLore(stack, listOf("Mushroom blocks are harder to mine", "You need at least an iron axe for drops", "Drop rate is reduced"))
	}
}