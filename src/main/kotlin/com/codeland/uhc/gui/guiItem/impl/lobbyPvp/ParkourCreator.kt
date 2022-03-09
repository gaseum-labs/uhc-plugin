package com.codeland.uhc.gui.guiItem.impl.lobbyPvp

import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.gui.guiItem.GuiItem
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.PvpQueue
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ParkourCreator(index: Int) : GuiItem(index) {
	override fun onClick(player: Player, shift: Boolean) {
		if (!PvpQueue.enabled.get()) return

		/* already in an arena, do nothing */
		if (ArenaManager.playersArena(player.uniqueId) != null) return

		val existingParkour = ParkourArena.playersParkour(player.uniqueId)

		/* player owns an arena already, go there */
		if (existingParkour != null) {
			existingParkour.startPlayer(player, existingParkour.playerLocation(player))
		} else {
			ArenaManager.addArena(ParkourArena(arrayListOf(arrayListOf(player.uniqueId)), player.uniqueId))
		}

		player.closeInventory()
	}

	override fun getStack(): ItemStack {
		return ItemCreator.fromType(
			if (PvpQueue.enabled.get()) Material.HEAVY_WEIGHTED_PRESSURE_PLATE
			else Material.STONE_PRESSURE_PLATE
		).name(
			if (PvpQueue.enabled.get()) "${ChatColor.GREEN}Create Parkour"
			else "${ChatColor.GRAY}Parkour is closed"
		).create()
	}
}
