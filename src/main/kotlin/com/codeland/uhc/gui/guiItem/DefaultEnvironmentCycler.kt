package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiInventory
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DefaultEnvironmentCycler(uhc: UHC, index: Int) : GuiItem(uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		/* bad things happen if we allow this value */
		/* to change during the game */
		if (uhc.isGameGoing()) return

		val values = World.Environment.values()

		uhc.defaultEnvironment = values[(values.indexOf(uhc.defaultEnvironment) + 1) % values.size]
	}

	override fun getStack(): ItemStack {
		val stack = setName(ItemStack(when (uhc.defaultEnvironment) {
			World.Environment.NORMAL -> Material.GRASS_BLOCK
			World.Environment.NETHER -> Material.NETHERRACK
			else -> Material.END_STONE
		}), stateName("Environment", Util.environmentPrettyNames[World.Environment.values().indexOf(uhc.defaultEnvironment)]))
		return setLore(stack, listOf("Which dimension the UHC starts in"))
	}
}
