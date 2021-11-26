package com.codeland.uhc.event;

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.gui.ItemCreator
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Piglin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class Barter : Listener {
	val barterItemList = arrayOf(
		ItemCreator.regular(Material.ENCHANTED_BOOK).customMeta<EnchantmentStorageMeta> {
			it.addStoredEnchant(Enchantment.SOUL_SPEED, 2, true)
		},
		ItemCreator.regular(Material.IRON_BOOTS).enchant(Enchantment.SOUL_SPEED, 2),
		Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.FIRE_RESISTANCE, false, false)),
		Brew.createDefaultPotion(Material.POTION, PotionData(PotionType.FIRE_RESISTANCE, false, false)),
		Brew.createDefaultPotion(Material.POTION, PotionData(PotionType.WATER, false, false)),
		ItemCreator.regular(Material.IRON_NUGGET).amount(27),
		ItemCreator.regular(Material.ENDER_PEARL).amount(3),
		ItemCreator.regular(Material.STRING).amount(6),
		ItemCreator.regular(Material.QUARTZ).amount(8),
		ItemCreator.regular(Material.OBSIDIAN),
		ItemCreator.regular(Material.CRYING_OBSIDIAN).amount(2),
		ItemCreator.regular(Material.FIRE_CHARGE),
		ItemCreator.regular(Material.LEATHER).amount(3),
		ItemCreator.regular(Material.SOUL_SAND).amount(6),
		ItemCreator.regular(Material.NETHER_BRICK).amount(8),
		ItemCreator.regular(Material.SPECTRAL_ARROW).amount(9),
		ItemCreator.regular(Material.GRAVEL).amount(12),
		ItemCreator.regular(Material.BLACKSTONE).amount(12),
	)

	val META_TAG = "_UHC_BARTER"

	inner class BarterInfo {
		var index = 1

		val indexList = Array(barterItemList.size) { it }

		init {
			indexList.shuffle()
		}
	}

	private fun getIndex(player: Player): Int {
		val meta = player.getMetadata(META_TAG)

		/* returns the index found before function was called */
		/* modifies the index to be +1 of that after function exits */
		return if (meta.size == 0) {
			val barterInfo = BarterInfo()
			player.setMetadata(META_TAG, FixedMetadataValue(UHCPlugin.plugin, barterInfo))

			barterInfo.indexList[0]

		} else {
			val barterInfo = meta[0].value() as BarterInfo
			val ret = barterInfo.indexList[barterInfo.index]

			if (++barterInfo.index >= barterInfo.indexList.size) {
				barterInfo.index = 0
				barterInfo.indexList.shuffle()
			}

			ret
		}
	}

	@EventHandler
	fun onPiglinDropItem(event: EntityDropItemEvent) {
		/* only called when piglins barter */
		if (event.entity !is Piglin) return

		/* the nearest player is (hopefully) the one who initiated the barter */
		var nearestDistance = Double.MAX_VALUE
		var nearestPlayer: Player? = null

		event.entity.world.players.forEach { player ->
			val playerData = PlayerData.getPlayerData(player.uniqueId)

			if (playerData.participating) {
				val distance = player.location.distance(event.entity.location)

				if (distance < nearestDistance) {
					nearestDistance = distance
					nearestPlayer = player
				}
			}
		}

		/* would be weird if no player was found but ok */
		val foundPlayer = nearestPlayer ?: return

		/* replace bartered item */
		val droppedItem = event.itemDrop
		droppedItem.itemStack = barterItemList[getIndex(foundPlayer)].create()
	}
}
