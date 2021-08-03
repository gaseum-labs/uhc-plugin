package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.ArenaType
import com.codeland.uhc.lobbyPvp.PvpQueue
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Item
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
			if (property.get() == -1) return
			if (ArenaManager.playersArena(player.uniqueId) != null) return

			val arena = ArenaManager.typeList<ParkourArena>(ArenaType.PARKOUR)[property.get()]
			arena.startPlayer(player, arena.playerLocation(player.uniqueId))

			player.closeInventory()
		}
	}

	override fun getStackProperty(value: Int): ItemStack {
		return if (value == -1) {
			name(ItemStack(Material.OAK_PRESSURE_PLATE), "${ChatColor.GRAY}No parkour lobbies")

		} else {
			val arena = ArenaManager.typeList<ParkourArena>(ArenaType.PARKOUR)[property.get()]

			val stack = ItemStack(Material.PLAYER_HEAD)
			val meta = stack.itemMeta as SkullMeta

			val player = Bukkit.getOfflinePlayer(arena.owner)

			meta.owningPlayer = Bukkit.getOfflinePlayer(arena.owner)
			meta.displayName(Component.text("${ChatColor.YELLOW}Join ${player.name}'s Parkour"))
			meta.lore(listOf(Component.text("Shift click to see more arenas")))

			stack.itemMeta = meta
			stack
		}
	}
}
