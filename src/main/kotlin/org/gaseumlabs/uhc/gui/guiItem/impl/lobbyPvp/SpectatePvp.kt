package org.gaseumlabs.uhc.gui.guiItem.impl.lobbyPvp;

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItem
import org.gaseumlabs.uhc.lobbyPvp.Arena
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SpectatePvp(index: Int) : GuiItem(index) {
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
			.name(Component.text("Spectate", BLUE))
			.lore(Component.text("Click to spectate lobby pvp"))
			.create()
	}
}
