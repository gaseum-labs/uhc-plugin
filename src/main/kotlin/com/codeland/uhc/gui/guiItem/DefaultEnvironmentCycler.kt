package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DefaultEnvironmentCycler(index: Int) : GuiItem(index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		/* bad things happen if we allow this value */
		/* to change during the game */
		if (UHC.isGameGoing()) return

		UHC.defaultWorldEnvironment =
		if (UHC.defaultWorldEnvironment == World.Environment.NORMAL)
			World.Environment.NETHER
		else
			World.Environment.NORMAL
	}

	override fun getStack(): ItemStack {
		val data = if (UHC.defaultWorldEnvironment == World.Environment.NORMAL)
			Pair(Material.GRASS_BLOCK, "Normal")
		else
			Pair(Material.NETHERRACK, "Nether")

		val stack = setName(ItemStack(data.first), stateName("World", data.second))
		return setLore(stack, listOf("Which dimension the UHC starts in"))
	}
}
