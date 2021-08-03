package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import com.codeland.uhc.lobbyPvp.PvpQueue
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ParkourCreator(index: Int) : GuiItem(index) {
	override fun onClick(player: Player, shift: Boolean) {
		/* already in an arena, do nothing */
		if (ArenaManager.playersArena(player.uniqueId) != null) return

		val existingParkour = ParkourArena.playersParkour(player.uniqueId)

		/* player owns an arena already, go there */
		if (existingParkour != null) {
			existingParkour.enterPlayer(player, false)
		} else {
			ArenaManager.addArena(ParkourArena(arrayListOf(arrayListOf(player.uniqueId))))
		}

		player.closeInventory()
	}

	override fun getStack(): ItemStack {
		return name(ItemStack(
			if (PvpQueue.enabled.get())
				Material.HEAVY_WEIGHTED_PRESSURE_PLATE
			else
				Material.STONE_PRESSURE_PLATE),
			if (PvpQueue.enabled.get())
				"${ChatColor.GREEN}Create Parkour"
			else
				"${ChatColor.GRAY}Parkour is closed"
		)
	}
}
