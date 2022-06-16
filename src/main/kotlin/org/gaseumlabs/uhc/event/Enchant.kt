package org.gaseumlabs.uhc.event

import org.gaseumlabs.uhc.core.PlayerData
import org.bukkit.Material
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.inventory.*
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.event.Enchant2.EnchantItem
import org.gaseumlabs.uhc.event.Enchant2.getSlotEnchantPreview
import org.gaseumlabs.uhc.event.Enchant2.getSlotEnchantments
import kotlin.random.Random

class Enchant : Listener {
	companion object {
		var seed = Random.nextLong()
	}

	@EventHandler
	fun onPrepareEnchant(event: PrepareItemEnchantEvent) {
		val playerData = PlayerData.getPlayerData(event.enchanter.uniqueId)
		playerData.shelves = event.enchantmentBonus
		val generatedOffers = createOffers(event.item, event.enchantmentBonus, playerData.enchantCycle)

		if (generatedOffers != null) {
			for (i in 0..2) {
				(event.offers as Array<EnchantmentOffer?>)[i] = generatedOffers[i]
			}
		}

		playerData.enchantEventFired = true
	}

	@EventHandler
	fun onGiveEnchants(event: EnchantItemEvent) {
		val playerData = PlayerData.getPlayerData(event.enchanter.uniqueId)

		val (enchantItem, itemId) = EnchantItem.get(event.item) ?: return

		val slotEnchants = getSlotEnchantments(
			seed,
			playerData.enchantCycle,
			itemId,
			event.whichButton(),
			playerData.shelves,
			enchantItem,
			true
		) ?: return

		event.enchantsToAdd.clear()
		slotEnchants.forEach { slotEnchant ->
			event.enchantsToAdd[slotEnchant.enchant] = slotEnchant.level
		}

		++playerData.enchantCycle
	}

	private fun createOffers(item: ItemStack, shelves: Int, cycle: Int): List<EnchantmentOffer?>? {
		val (enchantItem, itemId) = EnchantItem.get(item) ?: return null

		return arrayOf(
			getSlotEnchantPreview(seed, cycle, itemId, 0, shelves, enchantItem),
			getSlotEnchantPreview(seed, cycle, itemId, 1, shelves, enchantItem),
			getSlotEnchantPreview(seed, cycle, itemId, 2, shelves, enchantItem),
		).map { slot ->
			if (slot == null) {
				null
			} else {
				EnchantmentOffer(
					slot.enchant,
					slot.level,
					slot.requiredLevel
				)
			}
		}
	}
}
