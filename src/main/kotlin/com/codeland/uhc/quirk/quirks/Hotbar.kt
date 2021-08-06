package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.event.Chat
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

class Hotbar(type: QuirkType) : Quirk(type) {
	override fun onEnable() {}

	override fun onDisable() {}

	override val representation = ItemCreator.fromType(Material.BLACK_STAINED_GLASS_PANE)

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			for (slot in 9 until 36)
				player.inventory.setItem(slot, createUnusableSlot())
		}
	}

	override fun onEnd(uuid: UUID) {
		Util.log("ENDING FOR: ${uuid}")
		GameRunner.playerAction(uuid) { player ->
			for (slot in 9 until 36)
				player.inventory.setItem(slot, null)
		}
	}

	companion object {
		fun createUnusableSlot(): ItemStack {
			val itemStack = ItemStack(Material.BLACK_STAINED_GLASS_PANE)

			val meta = itemStack.itemMeta
			meta.setDisplayName("${ChatColor.RESET}${ChatColor.DARK_PURPLE}${ChatColor.BOLD}Unusable Slot")
			itemStack.itemMeta = meta

			return itemStack
		}

		fun filterDrops(drops: MutableList<ItemStack>) {
			drops.removeAll { itemStack ->
				itemStack.type == Material.BLACK_STAINED_GLASS_PANE && itemStack.itemMeta.hasDisplayName()
			}
		}
	}
}