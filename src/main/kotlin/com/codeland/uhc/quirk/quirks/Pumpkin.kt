package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.util.*

class Pumpkin(type: QuirkType) : Quirk(type) {
	override fun onEnable() {}

	override fun customDestroy() {}

	override val representation = ItemCreator.fromType(Material.CARVED_PUMPKIN)

	override fun onStartPlayer(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			val pumpkinItem = ItemStack(Material.CARVED_PUMPKIN)
			val meta = pumpkinItem.itemMeta
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
			pumpkinItem.itemMeta = meta

			player.inventory.helmet = pumpkinItem
		}
	}

	override fun onEndPlayer(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			player.inventory.helmet = null
		}
	}
}