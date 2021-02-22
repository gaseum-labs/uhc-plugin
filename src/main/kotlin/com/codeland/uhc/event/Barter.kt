package com.codeland.uhc.event;

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Piglin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class Barter : Listener {
	fun createPotion(material: Material, potionData: PotionData): ItemStack {
		val potion = ItemStack(material)
		val meta = potion.itemMeta as PotionMeta
		meta.basePotionData = potionData
		potion.itemMeta = meta
		return potion
	}

	val barterItemList = arrayOf(
		{
			val book = ItemStack(Material.ENCHANTED_BOOK)
			val meta = book.itemMeta as EnchantmentStorageMeta
			meta.addStoredEnchant(Enchantment.SOUL_SPEED, 2, true)
			book.itemMeta = meta
			book
		}, {
			val boots = ItemStack(Material.IRON_BOOTS)
			val meta = boots.itemMeta
			meta.addEnchant(Enchantment.SOUL_SPEED, 2, true)
			boots.itemMeta = meta
			boots
		}, {
			createPotion(Material.SPLASH_POTION, PotionData(PotionType.FIRE_RESISTANCE, false, false))
	    }, {
			createPotion(Material.POTION, PotionData(PotionType.FIRE_RESISTANCE, false, false))
		}, {
			createPotion(Material.POTION, PotionData(PotionType.WATER, false, false))
		}, {
			ItemStack(Material.IRON_NUGGET, 27)
		}, {
			ItemStack(Material.ENDER_PEARL, 3)
		}, {
			ItemStack(Material.STRING, 6)
		}, {
			ItemStack(Material.QUARTZ, 8)
		}, {
			ItemStack(Material.OBSIDIAN)
		}, {
			ItemStack(Material.CRYING_OBSIDIAN, 2)
		}, {
			ItemStack(Material.FIRE_CHARGE)
		}, {
			ItemStack(Material.LEATHER, 3)
		}, {
			ItemStack(Material.SOUL_SAND, 6)
		}, {
			ItemStack(Material.NETHER_BRICK, 8)
		}, {
			ItemStack(Material.SPECTRAL_ARROW, 9)
		}, {
			ItemStack(Material.GRAVEL, 12)
		}, {
			ItemStack(Material.BLACKSTONE, 12)
		}
	)

	val META_TAG = "_UHC_BARTER"

	data class BarterInfo(var index: Int, var indexList: Array<Int>)

	fun genIndexList(): Array<Int> {
		val ret = Array(barterItemList.size) { i -> i }

		Util.shuffleArray(ret)

		return ret
	}

	fun getIndex(player: Player): Int {
		val meta = player.getMetadata(META_TAG)

		return if (meta.size == 0) {
			val indexList = genIndexList()
			player.setMetadata(META_TAG, FixedMetadataValue(UHCPlugin.plugin, BarterInfo(0, indexList)))
			indexList[0]

		} else {
			val barterInfo = meta[0].value() as BarterInfo

			if (++barterInfo.index == barterInfo.indexList.size) {
				barterInfo.index = 0
				Util.shuffleArray(barterInfo.indexList)
			}

			barterInfo.indexList[barterInfo.index]
		}
	}

	@EventHandler
	fun onDropItemP(event: EntityDropItemEvent) {
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
		droppedItem.itemStack = barterItemList[getIndex(foundPlayer)]()
	}
}
