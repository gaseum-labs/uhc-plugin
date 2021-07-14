package com.codeland.uhc.event

import com.codeland.uhc.core.PlayerData
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

class Enchant : Listener {
	@EventHandler
	fun onGiveEnchants(event: PrepareItemEnchantEvent) {
		val bonus = event.enchantmentBonus.coerceAtMost(15)

		val playerData = PlayerData.getPlayerData(event.enchanter.uniqueId)
		val offerReturn = createOffers(event.item, bonus, playerData.enchantCycle)

		if (offerReturn == null) {
			playerData.storedOffers[0] = null
			playerData.storedOffers[1] = null
			playerData.storedOffers[2] = null

		} else {
			val (generatedOffers, type, hash) = offerReturn

			for (i in 0..2) {
				playerData.storedOffers[i] = generatedOffers[i]
				event.offers[i] = generatedOffers[i]!!
			}
			playerData.storedType = type
			playerData.storedShelves = bonus
			playerData.storedHash = hash
		}
	}

	@EventHandler
	fun onEnchant(event: EnchantItemEvent) {
		val playerData = PlayerData.getPlayerData(event.enchanter.uniqueId)

		val offer = playerData.storedOffers[event.whichButton()]
		if (offer != null) {
			event.enchantsToAdd.clear()
			event.enchantsToAdd[offer.enchantment] = offer.enchantmentLevel

			val type = playerData.storedType
			val shelves = playerData.storedShelves
			val random = random(playerData.enchantCycle, type.ordinal, shelves, playerData.storedHash)

			val extraEnchants = ArrayList<Pair<Enchantment, Int>>()

			for (i in 1..2) {
				val extra = grabExtraEnchant(type, extraEnchants, shelves - i * 5, random)
				if (extra != null) extraEnchants.add(extra)
			}

			extraEnchants.forEach { (enchantment, level) -> event.enchantsToAdd[enchantment] = level }
		}

		++playerData.enchantCycle
	}

	fun random(cycle: Int, typeNo: Int, bonus: Int, hash: Int): Random {
		return Random(
			(cycle * EnchantType.TYPE_SIZE * EnchantType.SHELVES_SIZE * EnchantType.ITEMS_SIZE) +
				(typeNo * EnchantType.SHELVES_SIZE * EnchantType.ITEMS_SIZE) +
				(bonus * EnchantType.ITEMS_SIZE) +
				hash
		)
	}

	fun grabExtraEnchant(type: EnchantType, exclude: ArrayList<Pair<Enchantment, Int>>, bonus: Int, random: Random): Pair<Enchantment, Int>? {
		if (bonus < 0) return null

		return type.options.map { option ->
			Pair(option.enchantment, option.getLevel(bonus))
		}.shuffled(random).firstOrNull { pair ->
			pair.second > 0 && exclude.none { pair.first === it.first }
		}
	}

	fun createOffers(item: ItemStack, bonus: Int, cycle: Int): Triple<ArrayList<EnchantmentOffer?>, EnchantType, Int>? {
		val (type, hash) = EnchantType.get(item.type)
		if (type == null) return null

		val random = random(cycle, type.ordinal, bonus, hash)

		val options = type.options.copyOf()
		options.shuffle(random)

		fun findOfferIndex(options: Array<EnchantOption>, startIndex: Int, shelves: Int): Int {
			for (i in startIndex until options.size) {
				if (options[i].getLevel(shelves) != 0) {
					return i
				}
			}

			return -1
		}

		val availables = arrayOf(
			Pair((bonus / 3.0f).roundToInt().coerceAtLeast(1), floor(bonus / 2.0f).toInt().coerceAtLeast(1)),
			Pair((bonus / 2.0f).roundToInt().coerceAtLeast(1), bonus.coerceAtLeast(1)),
			Pair(bonus.coerceAtLeast(1), (bonus * 2).coerceAtLeast(1))
		).map { (bonus, cost) ->
			options.mapNotNull {
				val level = it.getLevel(bonus)
				if (level > 0) {
					EnchantmentOffer(it.enchantment, level, cost)
				} else {
					null
				}
			}
		}

		val collect = arrayListOf(availables[0].firstOrNull())
		for (i in 1..2) {
			collect.add(availables[i].find { offer -> collect.all { offer.enchantment !== it?.enchantment } })
		}

		return Triple(collect, type, hash)
	}

	data class EnchantOption(val enchantment: Enchantment, val levels: Array<Int>) {
		fun getLevel(shelves: Int): Int {
			return levels[floor(shelves / 16.0f * levels.size).toInt()]
		}
	}

	enum class EnchantType(val items: Array<Material>, val options: Array<EnchantOption>) {
		SWORD(arrayOf(
			Material.IRON_SWORD,
			Material.DIAMOND_SWORD,
			Material.NETHERITE_SWORD
		), arrayOf(
			EnchantOption(Enchantment.DURABILITY,      arrayOf(1, 2, 3, 0, 0, 0)),
			EnchantOption(Enchantment.SWEEPING_EDGE,   arrayOf(1, 2, 3, 0, 0, 0)),
			EnchantOption(Enchantment.KNOCKBACK,       arrayOf(1, 2, 0)),
			EnchantOption(Enchantment.DAMAGE_ALL,      arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.LOOT_BONUS_MOBS, arrayOf(0, 1, 2, 3)),
			EnchantOption(Enchantment.FIRE_ASPECT,     arrayOf(0, 1, 2))
		)),
		TOOL(arrayOf(
			Material.IRON_AXE,
			Material.DIAMOND_AXE,
			Material.NETHERITE_AXE,
			Material.IRON_SHOVEL,
			Material.DIAMOND_SHOVEL,
			Material.NETHERITE_SHOVEL,
			Material.IRON_PICKAXE,
			Material.DIAMOND_PICKAXE,
			Material.NETHERITE_PICKAXE
		), arrayOf(
			EnchantOption(Enchantment.DURABILITY,        arrayOf(1, 2, 3, 3)),
			EnchantOption(Enchantment.LOOT_BONUS_BLOCKS, arrayOf(0, 0, 1, 2, 3)),
			EnchantOption(Enchantment.DIG_SPEED,         arrayOf(1, 2, 3, 4)),
		)),
		BOW(arrayOf(
			Material.BOW
		), arrayOf(
			EnchantOption(Enchantment.DURABILITY,      arrayOf(1, 2, 3, 0, 0, 0)),
			EnchantOption(Enchantment.ARROW_DAMAGE,    arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.ARROW_KNOCKBACK, arrayOf(1, 2, 3)),
			EnchantOption(Enchantment.ARROW_INFINITE,  arrayOf(0, 0, 0, 1)),
			EnchantOption(Enchantment.ARROW_FIRE,      arrayOf(0, 0, 0, 1)),
		)),
		CROSSBOW(arrayOf(
			Material.CROSSBOW
		), arrayOf(
			EnchantOption(Enchantment.DURABILITY,   arrayOf(1, 2, 3, 0, 0, 0)),
			EnchantOption(Enchantment.PIERCING,     arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.QUICK_CHARGE, arrayOf(0, 1, 2, 3)),
			EnchantOption(Enchantment.MULTISHOT,    arrayOf(0, 0, 1)),
		)),
		TRIDENT(arrayOf(
			Material.TRIDENT
		), arrayOf(
			EnchantOption(Enchantment.DURABILITY, arrayOf(1, 2, 3, 0, 0, 0)),
			EnchantOption(Enchantment.LOYALTY,    arrayOf(1, 2, 3)),
			EnchantOption(Enchantment.CHANNELING, arrayOf(0, 1, 2, 3)),
			EnchantOption(Enchantment.RIPTIDE,    arrayOf(0, 1, 2, 3)),
		)),
		ARMOR(arrayOf(
			Material.IRON_HELMET,
			Material.IRON_CHESTPLATE,
			Material.IRON_LEGGINGS,
			Material.IRON_BOOTS,
			Material.DIAMOND_HELMET,
			Material.DIAMOND_CHESTPLATE,
			Material.DIAMOND_LEGGINGS,
			Material.DIAMOND_BOOTS,
			Material.NETHERITE_HELMET,
			Material.NETHERITE_CHESTPLATE,
			Material.NETHERITE_LEGGINGS,
			Material.NETHERITE_BOOTS,
		), arrayOf(
			EnchantOption(Enchantment.DURABILITY,               arrayOf(1, 2, 3, 0, 0, 0)),
			EnchantOption(Enchantment.PROTECTION_ENVIRONMENTAL, arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.PROTECTION_PROJECTILE,    arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.PROTECTION_FIRE,          arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.THORNS,                   arrayOf(0, 1, 2, 3)),
		)),
		BOOK(arrayOf(
			Material.BOOK
		), arrayOf(
			EnchantOption(Enchantment.DURABILITY,               arrayOf(1, 2, 3, 0, 0, 0)),
			EnchantOption(Enchantment.PROTECTION_ENVIRONMENTAL, arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.PROTECTION_PROJECTILE,    arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.PROTECTION_FIRE,          arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.THORNS,                   arrayOf(0, 1, 2, 3)),
			EnchantOption(Enchantment.LOYALTY,                  arrayOf(1, 2, 3)),
			EnchantOption(Enchantment.CHANNELING,               arrayOf(0, 1, 2, 3)),
			EnchantOption(Enchantment.RIPTIDE,                  arrayOf(0, 1, 2, 3)),
			EnchantOption(Enchantment.PIERCING,                 arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.QUICK_CHARGE,             arrayOf(0, 1, 2, 3)),
			EnchantOption(Enchantment.MULTISHOT,                arrayOf(0, 0, 1)),
			EnchantOption(Enchantment.ARROW_DAMAGE,             arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.ARROW_KNOCKBACK,          arrayOf(1, 2, 3)),
			EnchantOption(Enchantment.ARROW_INFINITE,           arrayOf(0, 0, 0, 1)),
			EnchantOption(Enchantment.ARROW_FIRE,               arrayOf(0, 0, 0, 1)),
			EnchantOption(Enchantment.LOOT_BONUS_BLOCKS,        arrayOf(0, 0, 1, 2, 3)),
			EnchantOption(Enchantment.DIG_SPEED,                arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.SWEEPING_EDGE,            arrayOf(1, 2, 3, 0, 0, 0)),
			EnchantOption(Enchantment.KNOCKBACK,                arrayOf(1, 2, 0)),
			EnchantOption(Enchantment.DAMAGE_ALL,               arrayOf(1, 2, 3, 4)),
			EnchantOption(Enchantment.LOOT_BONUS_MOBS,          arrayOf(0, 1, 2, 3)),
			EnchantOption(Enchantment.FIRE_ASPECT,              arrayOf(0, 1, 2))
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
