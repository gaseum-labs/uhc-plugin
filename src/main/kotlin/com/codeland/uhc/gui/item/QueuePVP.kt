package com.codeland.uhc.gui.item

import com.codeland.uhc.core.UHC
import com.codeland.uhc.lobbyPvp.PvpData
import com.codeland.uhc.lobbyPvp.PvpQueue
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QueuePVP : CommandItem() {
	val MATERIAL = Material.IRON_SWORD

	override fun create(): ItemStack {
		val stack = ItemStack(MATERIAL)
		val meta = stack.itemMeta

		meta.setDisplayName("${ChatColor.RESET}${ChatColor.RED}Queue for PVP")
		meta.lore = listOf("Right click to to join the lobby PVP queue")

		stack.itemMeta = meta
		return stack
	}

	override fun isItem(stack: ItemStack): Boolean {
		return stack.type == MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
	}

	override fun onUse(uhc: UHC, player: Player) {
		if (PvpQueue.queueTime(player.uniqueId) == null) {
			PvpQueue.add(player.uniqueId)
			player.sendMessage("${ChatColor.RED}Entered PVP Queue")
		} else {
			PvpQueue.remove(player.uniqueId)
			player.sendMessage("${ChatColor.RED}Left PVP Queue")
		}
	}
}
