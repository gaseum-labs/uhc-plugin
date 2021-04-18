package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DefaultEnvironmentCycler(index: Int) : GuiItem(index, true) {
	private val representations = arrayOf(
		Material.GRASS_BLOCK,
		Material.NETHERRACK,
		Material.END_STONE
	)

	override fun onClick(player: Player, shift: Boolean) {
		/* bad things happen if we allow this value */
		/* to change during the game */
		if (UHC.isGameGoing()) return

		UHC.defaultWorldIndex = (UHC.defaultWorldIndex + 1) % 3
	}

	override fun getStack(): ItemStack {
		val stack = setName(ItemStack(representations[UHC.defaultWorldIndex]), stateName("World", Util.worldPrettyNames[UHC.defaultWorldIndex]))
		return setLore(stack, listOf("Which dimension the UHC starts in"))
	}
}
