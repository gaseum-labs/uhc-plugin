package org.gaseumlabs.uhc.event

import org.gaseumlabs.uhc.core.PlayerData
import org.bukkit.Material
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.event.Enchant2.EnchantItem
import org.gaseumlabs.uhc.event.Enchant2.getSlotEnchantPreview
import org.gaseumlabs.uhc.event.Enchant2.getSlotEnchantments
import kotlin.random.Random

class Enchant : Listener {
	private fun seed(): Long {
		return UHC.game?.world?.seed ?: Random.nextLong()
	}

	@EventHandler
	fun onPrepareEnchant(event: PrepareItemEnchantEvent) {
		val playerData = PlayerData.get(event.enchanter.uniqueId)
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
		val playerData = PlayerData.get(event.enchanter.uniqueId)

		val (enchantItem, itemId) = EnchantItem.get(event.item) ?: return

		val slotEnchants = getSlotEnchantments(
			seed(),
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
		val seed = seed()
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

	val blockToXp = hashMapOf(
		Material.COAL_ORE to 1,
		Material.NETHER_GOLD_ORE to 2,
		Material.NETHER_QUARTZ_ORE to 2,
		Material.LAPIS_ORE to 3,
		Material.REDSTONE_ORE to 3,
		Material.DIAMOND_ORE to 5,
		Material.EMERALD_ORE to 5,
	)

	val mobToXp = hashMapOf(
		EntityType.ZOMBIE to 13,
		EntityType.SPIDER to 13,
		EntityType.SKELETON to 13,
		EntityType.CREEPER to 13,
		EntityType.ENDERMAN to 17,
		EntityType.BLAZE to 17,
		EntityType.DROWNED to 17,
		EntityType.ZOMBIFIED_PIGLIN to 17,
		EntityType.HOGLIN to 26,
		EntityType.WITCH to 26,
		EntityType.GHAST to 26,
		EntityType.PHANTOM to 26,
	)

	@EventHandler
	fun onBlockDropXP(event: BlockBreakEvent) {
		val fixedXp = blockToXp[event.block.type]
		if (fixedXp != null) {
			event.expToDrop = fixedXp
		}
	}

	@EventHandler
	fun onMobKilled(event: EntityDeathEvent) {
		if (event.droppedExp == 0) return

		val fixedXp = mobToXp[event.entityType]
		if (fixedXp != null) {
			event.droppedExp = fixedXp
		}
	}
}
