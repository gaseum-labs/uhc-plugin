package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiInventory
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class AppleFixToggle(uhc: UHC, index: Int) : GuiItem(uhc, index, true) {
	override fun onClick(player: Player, shift: Boolean) {
		uhc.appleFix = !uhc.appleFix
	}

	override fun getStack(): ItemStack {
		val stack = setName(ItemStack(if (uhc.appleFix) Material.APPLE else Material.OAK_SAPLING), enabledName("Apple Fix", uhc.appleFix))
		return setLore(stack, listOf("Expect one apple in every 200 leaf blocks mined", "All types of leaves drop apples"))
	}
}