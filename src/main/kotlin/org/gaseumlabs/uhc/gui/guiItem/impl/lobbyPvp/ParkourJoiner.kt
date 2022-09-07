package org.gaseumlabs.uhc.gui.guiItem.impl.lobbyPvp

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.lobbyPvp.*
import org.gaseumlabs.uhc.lobbyPvp.arena.ParkourArena
import org.gaseumlabs.uhc.util.UHCProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import org.gaseumlabs.uhc.core.PlayerData

class ParkourJoiner(index: Int, val playerData: PlayerData) : GuiItemProperty<Int>(index) {
	override fun getProperty() = playerData::parkourIndex

	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Int>) {
		val arenaList = ArenaManager.ongoingOf<ParkourArena>()
		if (arenaList.isEmpty()) return

		if (shift) {
			property.set((property.get() + 1) % arenaList.size)
		} else {
			if (!PvpQueue.enabled) return
			if (property.get() == -1) return
			if (ArenaManager.playersArena(player.uniqueId) != null) return

			val arena = arenaList[property.get() % arenaList.size]
			arena.startPlayer(player, arena.playerLocation(player))

			player.closeInventory()
		}
	}

	override fun renderProperty(value: Int): ItemCreator {
		val arenaList = ArenaManager.ongoingOf<ParkourArena>()

		return if (arenaList.isEmpty()) {
			ItemCreator.display(Material.OAK_PRESSURE_PLATE)
				.name(Component.text("No parkour lobbies", GRAY))

		} else {
			val arena = arenaList[value % arenaList.size]
			val player = Bukkit.getOfflinePlayer(arena.owner)

			ItemCreator.display(Material.PLAYER_HEAD)
				.customMeta<SkullMeta> { it.owningPlayer = player }
				.name(Component.text("Join ${player.name}'s Parkour", YELLOW))
				.lore(if (arenaList.size == 1) emptyList() else listOf(Component.text("Shift click to see more arenas")))
		}
	}
}
