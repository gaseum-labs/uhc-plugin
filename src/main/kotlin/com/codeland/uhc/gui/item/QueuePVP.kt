package com.codeland.uhc.gui.item

import com.codeland.uhc.core.UHC
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.lobbyPvp.PvpQueue
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QueuePVP : CommandItem() {
	val MATERIAL = Material.IRON_SWORD

	override fun create(): ItemStack {
		val stack = ItemStack(MATERIAL)
		val meta = stack.itemMeta

		meta.displayName(Util.gradientString("Queue for PVP 1v1s", TextColor.color(0xe80e0e), TextColor.color(0xe8c00e)))
		meta.lore(listOf(Component.text("Right click to to join the 1v1 PVP queue")))

		stack.itemMeta = meta
		return stack
	}

	override fun isItem(stack: ItemStack): Boolean {
		return stack.type == MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
	}

	override fun onUse(uhc: UHC, player: Player) {
		val pvpGame = PvpGameManager.playersGame(player.uniqueId)

		if (pvpGame == null) {
			if (PvpQueue.queueTime(player.uniqueId) == null) {
				PvpQueue.add(player.uniqueId)
				player.sendMessage("${ChatColor.RED}Entered PVP Queue")

			} else {
				PvpQueue.remove(player.uniqueId)
				player.sendMessage("${ChatColor.GOLD}Left PVP Queue")
			}
		}
	}
}
