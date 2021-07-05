package com.codeland.uhc.gui.guiItem;

import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SpectatePvp(index: Int): GuiItem(index) {
	override fun onClick(player: Player, shift: Boolean) {
		if (PvpGameManager.playersGame(player.uniqueId) == null) {
			val center = PvpGameManager.ongoingGames.lastOrNull()?.getCenter()
				?: PvpGameManager.PvpGame.center(PvpGameManager.axes[0], PvpGameManager.axes[1])

			player.gameMode = GameMode.SPECTATOR
			player.teleport(Location(
				WorldManager.getPVPWorld(),
				center.first.toDouble(),
				128.0,
				center.second.toDouble()
			))
		}
	}

	override fun getStack(): ItemStack {
		return lore(
			name(ItemStack(Material.HEART_OF_THE_SEA), "${ChatColor.BLUE}Spectate"),
			listOf(Component.text("Click to spectate lobby pvp"))
		)
	}
}
