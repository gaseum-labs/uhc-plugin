package com.codeland.uhc.gui.item

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.phases.waiting.PvpData
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class JoinPvp : CommandItem() {
	val MATERIAL = Material.IRON_SWORD

	override fun create(): ItemStack {
		val stack = ItemStack(MATERIAL)
		val meta = stack.itemMeta

		meta.setDisplayName("${ChatColor.RESET}${ChatColor.RED}Join PVP")
		meta.lore = listOf("Right click to enter the lobby pvp arena")

		stack.itemMeta = meta
		return stack
	}

	override fun isItem(stack: ItemStack): Boolean {
		return stack.type == MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
	}

	override fun onUse(uhc: UHC, player: Player) {
		PvpData.enablePvp(player, true, true)
	}
}
