package com.codeland.uhc.gui.guiItem.impl.lobbyPvp;

import com.codeland.uhc.gui.guiItem.GuiItem
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.lobbyPvp.Arena
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.world.WorldManager
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SpectatePvp(index: Int): GuiItem(index) {
	override fun onClick(player: Player, shift: Boolean) {
		if (ArenaManager.playersArena(player.uniqueId) == null) {
			val center = ArenaManager.ongoing.lastOrNull()?.getCenter()
				?: Arena.getCenter(ArenaManager.spiral.getX(), ArenaManager.spiral.getZ())

			player.gameMode = GameMode.SPECTATOR
			player.teleport(Location(
				WorldManager.pvpWorld,
				center.first.toDouble(),
				128.0,
				center.second.toDouble()
			))
		}
	}

	override fun getStack(): ItemStack {
		return ItemCreator.fromType(Material.HEART_OF_THE_SEA)
			.name("${ChatColor.BLUE}Spectate")
			.lore("Click to spectate lobby pvp")
			.create()
	}
}
