package com.codeland.uhc.gui.guiItem.lobbyPvp

import com.codeland.uhc.util.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.ArenaType
import com.codeland.uhc.lobbyPvp.PvpQueue
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class ParkourJoiner(index: Int, parkourIndexProperty: UHCProperty<Int>) : GuiItemProperty<Int>(index, parkourIndexProperty) {
	override fun onClick(player: Player, shift: Boolean) {
		if (shift) {
			val arenaList = ArenaManager.typeList<ParkourArena>(ArenaType.PARKOUR)

			if (arenaList.isEmpty()) {
				property.set(-1)

			} else {
				property.set((property.get() + 1) % arenaList.size)
			}
		} else {
			if (!PvpQueue.enabled.get()) return
			if (property.get() == -1) return
			if (ArenaManager.playersArena(player.uniqueId) != null) return

			val arena = ArenaManager.typeList<ParkourArena>(ArenaType.PARKOUR)[property.get()]
			arena.startPlayer(player, arena.playerLocation(player))

			player.closeInventory()
		}
	}

	override fun getStackProperty(value: Int): ItemStack {
		return if (value == -1) {
			ItemCreator.fromType(Material.OAK_PRESSURE_PLATE).name("${ChatColor.GRAY}No parkour lobbies")

		} else {
			val arenaList = ArenaManager.typeList<ParkourArena>(ArenaType.PARKOUR)
			val arena = arenaList[property.get()]
			val player = Bukkit.getOfflinePlayer(arena.owner)

			ItemCreator.fromType(Material.PLAYER_HEAD)
				.customMeta { meta -> (meta as SkullMeta).owningPlayer = player }
				.name("${ChatColor.YELLOW}Join ${player.name}'s Parkour")
				.lore(if (arenaList.size == 1) "" else "Shift click to see more arenas")

		}.create()
	}
}
