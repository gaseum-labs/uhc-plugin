package com.codeland.uhc.event

import com.codeland.uhc.core.PlayerData
import net.minecraft.network.protocol.game.PacketPlayOutWindowData
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryView
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.inventory.*
import org.bukkit.inventory.EnchantingInventory
import org.bukkit.inventory.ItemStack

class Enchant : Listener {
	@EventHandler
	fun openEnchantingInventory(event: InventoryOpenEvent) {
		if (event.inventory.type === InventoryType.ENCHANTING) {
			val playerData = PlayerData.getPlayerData(event.player.uniqueId)
			playerData.storedOffers = emptyList()
			playerData.enchantEventFired = false
		}
	}

	@EventHandler
	fun onItemDragToEnchanting(event: InventoryDragEvent) {
		if (event.inventory.type === InventoryType.ENCHANTING && event.rawSlots.contains(0)) {
			val playerData = PlayerData.getPlayerData(event.whoClicked.uniqueId)
			playerData.storedOffers = emptyList()
			playerData.enchantEventFired = false
		}
	}

	@EventHandler
	fun onMoveItemToEnchanting(event: InventoryClickEvent) {
		if (event.inventory.type !== InventoryType.ENCHANTING) return
		val item = event.cursor ?: return

		/* putting item into the enchantment slot */
		/* taking item out of the enchantment table */
		if (
			(
				(
					event.action === InventoryAction.PLACE_ALL ||
					event.action === InventoryAction.PLACE_ONE ||
					event.action === InventoryAction.PLACE_SOME ||
					event.action === InventoryAction.SWAP_WITH_CURSOR
				) && (
					event.rawSlot == 0
				)
			) || (
				event.action === InventoryAction.MOVE_TO_OTHER_INVENTORY &&
				event.rawSlot >= 2 &&
				item.type !== Material.LAPIS_LAZULI
			) || (
				(
					event.action === InventoryAction.MOVE_TO_OTHER_INVENTORY ||
					event.action === InventoryAction.PICKUP_ALL ||
					event.action === InventoryAction.PICKUP_HALF ||
					event.action === InventoryAction.PICKUP_ONE ||
					event.action === InventoryAction.PICKUP_SOME
				) && event.rawSlot == 0
			)
		) {
			val playerData = PlayerData.getPlayerData(event.whoClicked.uniqueId)
			playerData.storedOffers = emptyList()
			playerData.enchantEventFired = false
		}
	}

	@EventHandler
	fun onPrepareEnchant(event: PrepareItemEnchantEvent) {
		val playerData = PlayerData.getPlayerData(event.enchanter.uniqueId)

		if (!playerData.enchantEventFired) ++playerData.enchantCycle

		val generatedOffers = createOffers(event.item, event.enchantmentBonus, playerData.enchantCycle)

		if (generatedOffers == null) {
			playerData.storedOffers = emptyList()
		} else {
			for (i in 0..2) {
				(event.offers as Array<EnchantmentOffer?>)[i] = generatedOffers[i]
			}

			playerData.storedOffers = generatedOffers
		}

		playerData.enchantEventFired = true
	}

	@EventHandler
	fun onGiveEnchants(event: EnchantItemEvent) {
		val playerData = PlayerData.getPlayerData(event.enchanter.uniqueId)

		if (playerData.storedOffers.isNotEmpty()) {
			val offer = playerData.storedOffers[event.whichButton()] ?: return

			event.enchantsToAdd.clear()
			event.enchantsToAdd[offer.enchantment] = offer.enchantmentLevel
		}
	}

	private fun createOffers(item: ItemStack, bonus: Int, cycle: Int): List<EnchantmentOffer?>? {
		val (type, _) = EnchantType.get(item.type)
		if (type == null) return null

		return arrayOf(
			tier(bonus) - 2,
			tier(bonus) - 1,
			tier(bonus),
		).mapIndexed { i, tier ->
			if (tier >= 0) {
				val optionsList = type.options.filter { it.getLevel(tier) > 0 }

				if (optionsList.isEmpty()) {
					null

				} else {
					val option = optionsList[(cycle + i) % optionsList.size]

					EnchantmentOffer(option.enchantment, option.getLevel(tier), tierCost(tier))
				}
			} else {
				null
			}
		}
	}

	fun tier(bonus: Int): Int {
		return (bonus + 1) / 2
	}

	fun tierCost(tier: Int):  Int {
		return ((2 * tier) - 1).coerceAtLeast(1)
	}

	data class EnchantOption(val enchantment: Enchantment, val levels: Array<Int>) {
		fun getLevel(tier: Int): Int {
			return levels[tier]
		}
	}

	companion object {
		val ENCHANT_OPTIONS_TOOL = arrayOf(
			EnchantOption(Enchantment.DURABILITY,        arrayOf(1, 1, 2, 2, 3, 3, 3, 3, 3)),
			EnchantOption(Enchantment.DIG_SPEED,         arrayOf(1, 1, 2, 2, 3, 3, 4, 4, 5)),
			EnchantOption(Enchantment.LOOT_BONUS_BLOCKS, arrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3)),
		)

		val ENCHANT_OPTIONS_ARMOR = arrayOf(
			EnchantOption(Enchantment.PROTECTION_ENVIRONMENTAL, arrayOf(1, 1, 1, 2, 2, 3, 3, 4, 4)),
			EnchantOption(Enchantment.PROTECTION_PROJECTILE,    arrayOf(1, 1, 1, 2, 2, 3, 3, 4, 4)),
			EnchantOption(Enchantment.PROTECTION_FIRE,          arrayOf(1, 1, 2, 2, 3, 3, 4, 4, 4)),
			EnchantOption(Enchantment.PROTECTION_EXPLOSIONS,    arrayOf(1, 1, 2, 2, 3, 3, 4, 4, 4)),
			EnchantOption(Enchantment.THORNS,                   arrayOf(0, 0, 0, 0, 1, 2, 2, 3, 3))
		)

		/**
		 * IMPORTANT! do not reorder
		 * https://wiki.vg/Protocol#Window_Property
		 */
		val packetEnchantmentIds = arrayOf(
			Enchantment.PROTECTION_ENVIRONMENTAL,
			Enchantment.PROTECTION_FIRE,
			Enchantment.PROTECTION_FALL,
			Enchantment.PROTECTION_EXPLOSIONS,
			Enchantment.PROTECTION_PROJECTILE,
			Enchantment.OXYGEN,
			Enchantment.WATER_WORKER,
			Enchantment.THORNS,
			Enchantment.DEPTH_STRIDER,
			Enchantment.FROST_WALKER,
			Enchantment.BINDING_CURSE,
			Enchantment.SOUL_SPEED,
			Enchantment.DAMAGE_ALL,
			Enchantment.DAMAGE_UNDEAD,
			Enchantment.DAMAGE_ARTHROPODS,
			Enchantment.KNOCKBACK,
			Enchantment.FIRE_ASPECT,
			Enchantment.LOOT_BONUS_MOBS,
			Enchantment.SWEEPING_EDGE,
			Enchantment.DIG_SPEED,
			Enchantment.SILK_TOUCH,
			Enchantment.DURABILITY,
			Enchantment.LOOT_BONUS_BLOCKS,
			Enchantment.ARROW_DAMAGE,
			Enchantment.ARROW_KNOCKBACK,
			Enchantment.ARROW_FIRE,
			Enchantment.ARROW_INFINITE,
			Enchantment.LUCK,
			Enchantment.LURE,
			Enchantment.LOYALTY,
			Enchantment.IMPALING,
			Enchantment.CHANNELING,
			Enchantment.MULTISHOT,
			Enchantment.QUICK_CHARGE,
			Enchantment.PIERCING,
			Enchantment.MENDING,
			Enchantment.VANISHING_CURSE,
		)
	}

	enum class EnchantType(val items: Array<Material>, val options: Array<EnchantOption>) {
		SWORD(arrayOf(
			Material.IRON_SWORD,
			Material.DIAMOND_SWORD,
			Material.NETHERITE_SWORD
		), arrayOf(
			EnchantOption(Enchantment.SWEEPING_EDGE,   arrayOf(1, 2, 3, 3, 3, 3, 3, 3, 3)),
			EnchantOption(Enchantment.KNOCKBACK,       arrayOf(1, 1, 1, 2, 2, 2, 2, 2, 2)),
			EnchantOption(Enchantment.DAMAGE_ALL,      arrayOf(1, 1, 2, 2, 2, 3, 3, 3, 4)),
			EnchantOption(Enchantment.LOOT_BONUS_MOBS, arrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3)),
			EnchantOption(Enchantment.FIRE_ASPECT,     arrayOf(0, 0, 0, 0, 1, 1, 2, 2, 2))
		)),
		AXE(arrayOf(
			Material.IRON_AXE,
			Material.DIAMOND_AXE,
			Material.NETHERITE_AXE
		), arrayOf(
			*ENCHANT_OPTIONS_TOOL,
			EnchantOption(Enchantment.DAMAGE_ALL,        arrayOf(0, 0, 0, 0, 0, 1, 2, 3, 4))
		)),
		TOOL(arrayOf(
			Material.IRON_SHOVEL,
			Material.DIAMOND_SHOVEL,
			Material.NETHERITE_SHOVEL,
			Material.IRON_PICKAXE,
			Material.DIAMOND_PICKAXE,
			Material.NETHERITE_PICKAXE
		), ENCHANT_OPTIONS_TOOL),
		BOW(arrayOf(
			Material.BOW
		), arrayOf(
			EnchantOption(Enchantment.ARROW_DAMAGE,    arrayOf(1, 1, 1, 2, 2, 3, 3, 3, 4)),
			EnchantOption(Enchantment.ARROW_KNOCKBACK, arrayOf(1, 1, 1, 2, 2, 2, 2, 2, 2)),
			EnchantOption(Enchantment.ARROW_INFINITE,  arrayOf(0, 0, 0, 0, 0, 0, 0, 1, 1)),
			EnchantOption(Enchantment.ARROW_FIRE,      arrayOf(0, 0, 0, 0, 0, 0, 0, 1, 1))
		)),
		CROSSBOW(arrayOf(
			Material.CROSSBOW
		), arrayOf(
			EnchantOption(Enchantment.PIERCING,     arrayOf(1, 1, 2, 2, 3, 3, 4, 4, 4)),
			EnchantOption(Enchantment.QUICK_CHARGE, arrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3)),
			EnchantOption(Enchantment.MULTISHOT,    arrayOf(0, 0, 0, 0, 1, 1, 1, 1, 1))
		)),
		TRIDENT(arrayOf(
			Material.TRIDENT
		), arrayOf(
			EnchantOption(Enchantment.LOYALTY,    arrayOf(1, 1, 1, 2, 2, 2, 3, 3, 3)),
			EnchantOption(Enchantment.RIPTIDE,    arrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3)),
			EnchantOption(Enchantment.CHANNELING, arrayOf(0, 0, 0, 1, 1, 1, 1, 1, 1))
		)),
		CHESTPLATE_LEGGINGS(arrayOf(
			Material.IRON_CHESTPLATE,
			Material.IRON_LEGGINGS,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.NETHERITE_CHESTPLATE,
			Material.NETHERITE_LEGGINGS,
		), ENCHANT_OPTIONS_ARMOR),
		HELMET(arrayOf(
			Material.IRON_HELMET,
			Material.DIAMOND_HELMET,
			Material.NETHERITE_HELMET,
		), arrayOf(
			*ENCHANT_OPTIONS_ARMOR,
			EnchantOption(Enchantment.WATER_WORKER, arrayOf(1, 2, 3, 3, 3, 3, 3, 3, 3)),
			EnchantOption(Enchantment.OXYGEN,       arrayOf(1, 2, 3, 3, 3, 3, 3, 3, 3)),
		)),
		BOOTS(arrayOf(
			Material.IRON_BOOTS,
			Material.DIAMOND_BOOTS,
			Material.NETHERITE_BOOTS,
		), arrayOf(
			*ENCHANT_OPTIONS_ARMOR,
			EnchantOption(Enchantment.DEPTH_STRIDER,   arrayOf(1, 2, 3, 3, 3, 3, 3, 3, 3)),
			EnchantOption(Enchantment.PROTECTION_FALL, arrayOf(1, 2, 3, 4, 4, 4, 4, 4, 4)),
		)),
		BOOK(arrayOf(
			Material.BOOK
		), arrayOf(
			EnchantOption(Enchantment.DIG_SPEED,                arrayOf(1, 1, 2, 2, 3, 3, 4, 4, 5)),
			EnchantOption(Enchantment.PROTECTION_ENVIRONMENTAL, arrayOf(1, 1, 1, 2, 2, 3, 3, 4, 4)),
			EnchantOption(Enchantment.PROTECTION_PROJECTILE,    arrayOf(1, 1, 1, 2, 2, 3, 3, 4, 4)),
			EnchantOption(Enchantment.ARROW_DAMAGE,             arrayOf(1, 1, 1, 2, 2, 3, 3, 3, 4)),
			EnchantOption(Enchantment.DAMAGE_ALL,               arrayOf(1, 1, 2, 2, 2, 3, 3, 3, 4)),
			EnchantOption(Enchantment.LOYALTY,                  arrayOf(1, 1, 1, 2, 2, 2, 3, 3, 3)),
			EnchantOption(Enchantment.LOOT_BONUS_BLOCKS,        arrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3)),
			EnchantOption(Enchantment.LOOT_BONUS_MOBS,          arrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3)),
			EnchantOption(Enchantment.THORNS,                   arrayOf(0, 0, 0, 0, 1, 2, 2, 3, 3)),
			EnchantOption(Enchantment.RIPTIDE,                  arrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3)),
			EnchantOption(Enchantment.QUICK_CHARGE,             arrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3))
		));

		companion object {
			val ITEMS_SIZE = values().map { it.items.size }.maxOrNull() ?: 0
			val SHELVES_SIZE = 16
			val TYPE_SIZE = values().size

			fun get(material: Material): Pair<EnchantType?, Int> {
				for (type in values()) {
					for (i in type.items.indices) {
						if (type.items[i] === material) {
							return Pair(type, i)
						}
					}
				}

				return Pair(null, 0)
			}
		}
	}
}
