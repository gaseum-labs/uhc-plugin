package org.gaseumlabs.uhc.gui.guiItem.impl.lobbyPvp

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItem
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.PvpQueue
import org.gaseumlabs.uhc.lobbyPvp.arena.ParkourArena
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ParkourCreator(index: Int) : GuiItem(index) {
	override fun onClick(player: Player, shift: Boolean) {
		if (!PvpQueue.enabled) return

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

	override fun render() =
		ItemCreator.display(
			if (PvpQueue.enabled) Material.HEAVY_WEIGHTED_PRESSURE_PLATE
			else Material.STONE_PRESSURE_PLATE
		).name(
			if (PvpQueue.enabled) Component.text("Create Parkour", GREEN)
			else Component.text("Parkour is closed", GRAY)
		)
}
