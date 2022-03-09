package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.Game
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Action
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

class Hotbar(type: QuirkType, game: Game) : Quirk(type, game) {
	override fun customDestroy() {}

	override fun onStartPlayer(uuid: UUID) {
		val creator = ItemCreator.fromType(Material.BLACK_STAINED_GLASS_PANE)
			.name("${ChatColor.DARK_PURPLE}${ChatColor.BOLD}Unusable Slot")

		Action.playerAction(uuid) { player ->
			for (slot in 9 until 36)
				player.inventory.setItem(slot, creator.create())
		}
	}

	override fun onEndPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player ->
			for (slot in 9 until 36)
				player.inventory.setItem(slot, null)
		}
	}

	fun filterDrops(drops: MutableList<ItemStack>) {
		drops.removeAll { itemStack ->
			itemStack.type == Material.BLACK_STAINED_GLASS_PANE && itemStack.itemMeta.hasDisplayName()
		}
	}
}
