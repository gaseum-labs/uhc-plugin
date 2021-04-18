package com.codeland.uhc.gui.item

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Spectate : CommandItem() {
	val MATERIAL = Material.HEART_OF_THE_SEA

	override fun create(): ItemStack {
		val stack = ItemStack(MATERIAL)
		val meta = stack.itemMeta

		meta.setDisplayName("${ChatColor.RESET}${ChatColor.BLUE}Spectate")
		meta.lore = listOf("Right click to spectate if you have died")

		stack.itemMeta = meta
		return stack
	}

	override fun isItem(stack: ItemStack): Boolean {
		return stack.type == MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
	}

	override fun onUse(uhc: UHC, player: Player) {
		if (PlayerData.isParticipating(player.uniqueId)) return

		if (!UHC.isPhase(PhaseType.WAITING)) {
			player.gameMode = GameMode.SPECTATOR
			player.setItemOnCursor(null)
			player.inventory.clear()
			player.teleport(UHC.spectatorSpawnLocation())

		} else {
			Commands.errorMessage(player, "Game has not started!")
		}
	}
}
