package org.gaseumlabs.uhc.event

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.Enchantment.*
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.util.Util
import kotlin.math.roundToInt
import kotlin.random.*

object Enchant2 {
	class SlotEnchant(
		val enchant: Enchantment,
		val level: Int,
		val requiredLevel: Int,
	)

	fun getRequiredLevelSlot(slot: Int, shelves: Int): Int {
		return when (slot) {
			0 -> (1.0f + (7.0f - 1.0f) / 15.0f * shelves).toInt()
			1 -> (3.0f + (15.0f - 3.0f) / 15.0f * shelves).toInt()
			else -> (5.0f + (30.0f - 5.0f) / 15.0f * shelves).toInt()
		}
	}

	fun getEnchantLevel(option: EnchantOption2, requiredLevel: Int, random: Random): Int {
		return when {
			requiredLevel <= 10 -> if (random.nextInt(10) < requiredLevel) option.l1 else option.l0
			requiredLevel <= 20 -> if (random.nextInt(10, 20) < requiredLevel) option.l2 else option.l1
			else -> if (random.nextInt(30) < requiredLevel) option.l3 else option.l2
		}
	}

	fun numSecondaryEnchants(requiredLevel: Int, random: Random): Int {
		return when {
			requiredLevel <= 10 -> if (random.nextInt(10) < requiredLevel)
				random.nextInt(0, 2) else 0
			requiredLevel <= 20 -> if (random.nextInt(10, 20) < requiredLevel)
				random.nextInt(0, 3) else random.nextInt(0, 2)
			else -> if (random.nextInt(30) < requiredLevel)
				random.nextInt(0, 4) else random.nextInt(0, 3)
		}
	}

	fun alreadyInList(
		slotEnchants: ArrayList<SlotEnchant>,
		option: EnchantOption2,
	): Boolean {
		return slotEnchants.any { it.enchant === option.enchantment }
	}

	fun getFromList(
		enchants: ArrayList<SlotEnchant>,
		list: Array<EnchantOption2>,
		requiredLevel: Int,
		random: Random,
	) {
		val initialIndex = random.nextInt(list.size)
		var index = initialIndex
		do {
			val option = list[index]
			if (alreadyInList(enchants, option)) {
				index = (index + 1) % list.size
				continue
			}

			val level = getEnchantLevel(option, requiredLevel, random)
			if (level == 0) {
				index = (index + 1) % list.size
				continue
			}

			enchants.add(SlotEnchant(option.enchantment, level, requiredLevel))
			return
		} while (index != initialIndex)
	}

	fun getSlotEnchantPreview(
		seed: Long,
		cycle: Int,
		itemId: Int,
		slot: Int,
		shelves: Int,
		enchantItem: EnchantItem,
	): SlotEnchant? {
		return getSlotEnchantments(
			seed,
			cycle,
			itemId,
			slot,
			shelves,
			enchantItem,
			false
		)?.first() ?: return null
	}

	fun getSlotEnchantments(
		seed: Long,
		cycle: Int,
		itemId: Int,
		slot: Int,
		shelves: Int,
		enchantItem: EnchantItem,
		full: Boolean,
	): ArrayList<SlotEnchant>? {
		val ret = ArrayList<SlotEnchant>(3)

		val random = Random(
			seed.and(0xffffffff).xor(cycle.toLong()).or(
				seed.and(0xffffffff.shl(32)).xor(
					slot.or(itemId.shl(16)).toLong().shl(32)
				)
			)
		)
		val requiredLevel = getRequiredLevelSlot(slot, shelves)

		/* get primary enchant */
		getFromList(ret, enchantItem.primaryEnchants, requiredLevel, random)

		var numSecondary = numSecondaryEnchants(requiredLevel, random)
		/* preview mode */
		if (ret.isEmpty() && !full) return ret
		/* somehow if no primary enchant, default to secondary enchant */
		if (numSecondary == 0 && ret.isEmpty()) numSecondary = 1
		for (i in 0 until numSecondary) {
			getFromList(
				ret,
				enchantItem.secondaryEnchants,
				(requiredLevel - random.nextInt((i + 1)..(i + 1) * 3)).coerceAtLeast(1),
				random
			)
		}

		return if (ret.isEmpty()) null else ret
	}

	class EnchantOption2(
		val enchantment: Enchantment,
		val l0: Int,
		val l1: Int,
		val l2: Int,
		val l3: Int,
	)

	enum class EnchantItem(
		val items: Array<Material>,
		val primaryEnchants: Array<EnchantOption2>,
		val secondaryEnchants: Array<EnchantOption2>,
	) {
		HELMET(
			arrayOf(
				Material.IRON_HELMET,
				Material.DIAMOND_HELMET,
				Material.NETHERITE_HELMET,
			), arrayOf(
				OPTION_PROTECTION,
				OPTION_PROJECTILE_PROTECTION,
				OPTION_AQUA_AFFINITY,
			), arrayOf(
				OPTION_BLAST_PROTECTION,
				OPTION_FIRE_PROTECTION,
				OPTION_RESPIRATION,
				OPTION_AQUA_AFFINITY
			)
		),
		CHESTPLATE_LEGGINGS(
			arrayOf(
				Material.IRON_CHESTPLATE,
				Material.IRON_LEGGINGS,
				Material.DIAMOND_CHESTPLATE,
				Material.DIAMOND_LEGGINGS,
				Material.NETHERITE_CHESTPLATE,
				Material.NETHERITE_LEGGINGS,
			), arrayOf(
				OPTION_PROTECTION,
				OPTION_PROJECTILE_PROTECTION,
			), arrayOf(
				OPTION_BLAST_PROTECTION,
				OPTION_FIRE_PROTECTION,
			)
		),
		BOOTS(
			arrayOf(
				Material.IRON_BOOTS,
				Material.DIAMOND_BOOTS,
				Material.NETHERITE_BOOTS,
			), arrayOf(
				OPTION_PROTECTION,
				OPTION_PROJECTILE_PROTECTION,
				OPTION_FEATHER_FALLING,
			), arrayOf(
				OPTION_DEPTH_STRIDER,
				OPTION_FEATHER_FALLING
			)
		),
		TOOL(
			arrayOf(
				Material.IRON_SHOVEL,
				Material.DIAMOND_SHOVEL,
				Material.NETHERITE_SHOVEL,
				Material.IRON_PICKAXE,
				Material.DIAMOND_PICKAXE,
				Material.NETHERITE_PICKAXE,
				Material.DIAMOND_AXE,
				Material.NETHERITE_AXE,
				Material.IRON_AXE,
			), arrayOf(
				OPTION_EFFICIENCY,
				OPTION_FORTUNE,
				OPTION_UNBREAKING,
			), arrayOf(
				OPTION_EFFICIENCY,
				OPTION_FORTUNE,
				OPTION_UNBREAKING,
			)
		),
		SWORD(
			arrayOf(
				Material.IRON_SWORD,
				Material.DIAMOND_SWORD,
				Material.NETHERITE_SWORD
			), arrayOf(
				OPTION_SHARPNESS,
				OPTION_FIRE_ASPECT,
				OPTION_LOOTING,
				OPTION_KNOCKBACK
			), arrayOf(
				OPTION_KNOCKBACK,
				OPTION_UNBREAKING,
				OPTION_SHARPNESS,
			)
		),
		BOW(
			arrayOf(
				Material.BOW
			),
			arrayOf(
				OPTION_POWER,
				OPTION_PUNCH,
				OPTION_FLAME,
				OPTION_INFINITY
			), arrayOf(
				OPTION_POWER,
				OPTION_UNBREAKING,
			)
		),
		CROSSBOW(
			arrayOf(
				Material.CROSSBOW
			),
			arrayOf(
				OPTION_PIERCING,
				OPTION_QUICK_CHARGE,
				OPTION_MULTISHOT,
			),
			arrayOf(
				OPTION_PIERCING,
				OPTION_UNBREAKING,
			)
		),
		TRIDENT(
			arrayOf(
				Material.TRIDENT
			),
			arrayOf(
				OPTION_RIPTIDE,
				OPTION_LOYALTY,
			),
			arrayOf(
				OPTION_UNBREAKING,
				OPTION_IMPALING,
			)
		);

		companion object {
			/* also the unique id of the item within the enchantItem */
			/* this way the seeded random can be different per item */
			fun get(itemStack: ItemStack): Pair<EnchantItem, Int>? {
				for (i in values().indices) {
					val enchantItem = values()[i]
					val subIndex = enchantItem.items.indexOf(itemStack.type)

					if (subIndex != -1) {
						return enchantItem to subIndex
					}
				}

				return null
			}
		}
	}

	val OPTION_PROTECTION = EnchantOption2(PROTECTION_ENVIRONMENTAL, 1, 2, 3, 4)
	val OPTION_PROJECTILE_PROTECTION = EnchantOption2(PROTECTION_PROJECTILE, 1, 2, 3, 4)

	val OPTION_BLAST_PROTECTION = EnchantOption2(PROTECTION_EXPLOSIONS, 1, 2, 3, 4)
	val OPTION_FIRE_PROTECTION = EnchantOption2(PROTECTION_FIRE, 1, 2, 3, 4)

	val OPTION_FEATHER_FALLING = EnchantOption2(PROTECTION_FALL, 1, 2, 3, 4)
	val OPTION_DEPTH_STRIDER = EnchantOption2(DEPTH_STRIDER, 1, 2, 3, 3)

	val OPTION_RESPIRATION = EnchantOption2(OXYGEN, 1, 2, 3, 3)
	val OPTION_AQUA_AFFINITY = EnchantOption2(WATER_WORKER, 1, 2, 3, 3)

	val OPTION_UNBREAKING = EnchantOption2(DURABILITY, 1, 2, 3, 3)

	val OPTION_SHARPNESS = EnchantOption2(DAMAGE_ALL, 1, 2, 3, 4)
	val OPTION_FIRE_ASPECT = EnchantOption2(FIRE_ASPECT, 0, 0, 1, 2)
	val OPTION_LOOTING = EnchantOption2(LOOT_BONUS_MOBS, 0, 1, 2, 3)
	val OPTION_KNOCKBACK = EnchantOption2(KNOCKBACK, 1, 2, 2, 2)

	val OPTION_EFFICIENCY = EnchantOption2(DIG_SPEED, 1, 2, 3, 4)
	val OPTION_FORTUNE = EnchantOption2(LOOT_BONUS_BLOCKS, 0, 1, 2, 3)

	val OPTION_POWER = EnchantOption2(ARROW_DAMAGE, 1, 2, 3, 4)
	val OPTION_PUNCH = EnchantOption2(ARROW_KNOCKBACK, 1, 2, 2, 2)
	val OPTION_FLAME = EnchantOption2(ARROW_FIRE, 0, 0, 1, 1)
	val OPTION_INFINITY = EnchantOption2(ARROW_INFINITE, 0, 0, 0, 1)

	val OPTION_PIERCING = EnchantOption2(PIERCING, 1, 2, 3, 4)
	val OPTION_QUICK_CHARGE = EnchantOption2(QUICK_CHARGE, 0, 1, 2, 3)
	val OPTION_MULTISHOT = EnchantOption2(MULTISHOT, 0, 0, 1, 1)

	val OPTION_RIPTIDE = EnchantOption2(RIPTIDE, 1, 2, 3, 3)
	val OPTION_LOYALTY = EnchantOption2(LOYALTY, 1, 2, 3, 3)
	val OPTION_IMPALING = EnchantOption2(IMPALING, 1, 2, 3, 4)
}
