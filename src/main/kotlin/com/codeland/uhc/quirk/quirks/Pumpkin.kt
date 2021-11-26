package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.Game
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Action
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.util.*

class Pumpkin(type: QuirkType, game: Game) : Quirk(type, game) {
	override fun customDestroy() {}

	override fun onStartPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player ->
			val pumpkinItem = ItemStack(Material.CARVED_PUMPKIN)
			val meta = pumpkinItem.itemMeta
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
			pumpkinItem.itemMeta = meta

			player.inventory.helmet = pumpkinItem
		}
	}

	override fun onEndPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player ->
			player.inventory.helmet = null
		}
	}
}