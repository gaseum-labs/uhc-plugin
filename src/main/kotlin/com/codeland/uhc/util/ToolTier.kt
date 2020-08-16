package com.codeland.uhc.util

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object ToolTier {
	const val WOOD = 0; const val LEATHER = 0
	const val GOLD = 1
	const val STONE = 2; const val CHAIN = 2
	const val IRON = 3
	const val DIAMOND = 4
	const val NETHERITE = 5
	const val SHELL = 6; const val SPECIAL = 6

	const val NUM_TIERS = 3
	const val NUM_MATERIALS = SPECIAL + 1

	const val TIER_1 = 0
	const val TIER_2 = 1
	const val TIER_3 = 2

	class EnchantmentPair {
		val enchantment: Enchantment
		val minLevel: Int
		val maxLevel: Int

		constructor(enchantment: Enchantment, minLevel: Int, maxLevel: Int) {
			this.enchantment = enchantment
			this.minLevel = minLevel
			this.maxLevel = maxLevel
		}

		constructor(enchantment: Enchantment, level: Int) {
			this.enchantment = enchantment
			this.minLevel = level
			this.maxLevel = level
		}

		constructor(enchantment: Enchantment) {
			this.enchantment = enchantment
			this.minLevel = 1
			this.maxLevel = 1
		}
	}

	class ToolEnchantmentTier(val available: Array<EnchantmentPair>, val option: Array<EnchantmentPair> = emptyArray())

	class ToolTieredInfo {
		val materials: Array<Material>
		val tiers: Array<ToolEnchantmentTier>

		constructor(materials: Array<Material>, tiers: Array<ToolEnchantmentTier>) {
			this.materials = materials
			this.tiers = tiers
		}

		constructor(material: Material, tiers: Array<ToolEnchantmentTier>) {
			this.materials = Array(NUM_MATERIALS) { material }
			this.tiers = tiers
		}

		constructor(material: Material, tier: ToolEnchantmentTier) {
			this.materials = Array(NUM_MATERIALS) { material }
			this.tiers = Array(NUM_TIERS) { tier }
		}
	}

	/* begin tiers */

	val TIER_SWORD = ToolTieredInfo(
		arrayOf(Material.WOODEN_SWORD, Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
		arrayOf(
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.LOOT_BONUS_MOBS, 2, 3),
				EnchantmentPair(Enchantment.SWEEPING_EDGE, 1, 2),
				EnchantmentPair(Enchantment.DAMAGE_ALL, 1)
			)),
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.SWEEPING_EDGE, 3),
				EnchantmentPair(Enchantment.DAMAGE_ALL, 2),
				EnchantmentPair(Enchantment.VANISHING_CURSE),
				EnchantmentPair(Enchantment.KNOCKBACK, 1)
			)),
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.DAMAGE_ALL, 3, 4),
				EnchantmentPair(Enchantment.VANISHING_CURSE),
				EnchantmentPair(Enchantment.KNOCKBACK, 2)
			))
		)
	)

	val TIER_AXE = ToolTieredInfo(
		arrayOf(Material.WOODEN_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE),
		arrayOf(
			ToolEnchantmentTier(
				arrayOf(
					EnchantmentPair(Enchantment.DIG_SPEED, 1, 4),
					EnchantmentPair(Enchantment.DURABILITY, 2, 3)
				),
				arrayOf(
					EnchantmentPair(Enchantment.LOOT_BONUS_BLOCKS, 1, 3),
					EnchantmentPair(Enchantment.SILK_TOUCH)
				)
			),
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.DIG_SPEED, 5),
				EnchantmentPair(Enchantment.DAMAGE_ALL, 1, 2),
				EnchantmentPair(Enchantment.VANISHING_CURSE)
			)),
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.DAMAGE_ALL, 3, 4),
				EnchantmentPair(Enchantment.VANISHING_CURSE)
			))
		)
	)

	private val miningTiers = arrayOf(
		ToolEnchantmentTier(
			arrayOf(
				EnchantmentPair(Enchantment.DIG_SPEED, 1, 4),
				EnchantmentPair(Enchantment.DURABILITY, 2, 3),
				EnchantmentPair(Enchantment.VANISHING_CURSE)
			),
			arrayOf(
				EnchantmentPair(Enchantment.LOOT_BONUS_BLOCKS, 1, 3),
				EnchantmentPair(Enchantment.SILK_TOUCH)
			)
		),
		ToolEnchantmentTier(
			arrayOf(
				EnchantmentPair(Enchantment.DIG_SPEED, 5),
				EnchantmentPair(Enchantment.DURABILITY, 3)
			),
			arrayOf(
				EnchantmentPair(Enchantment.LOOT_BONUS_BLOCKS, 1, 3),
				EnchantmentPair(Enchantment.SILK_TOUCH)
			)
		),
		ToolEnchantmentTier(
			arrayOf(
				EnchantmentPair(Enchantment.DIG_SPEED, 5),
				EnchantmentPair(Enchantment.DURABILITY, 3)
			),
			arrayOf(
				EnchantmentPair(Enchantment.LOOT_BONUS_BLOCKS, 1, 3),
				EnchantmentPair(Enchantment.SILK_TOUCH)
			)
		)
	)

	val TIER_PICKAXE = ToolTieredInfo(
		arrayOf(Material.WOODEN_PICKAXE, Material.GOLDEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE),
		miningTiers
	)

	val TIER_SHOVEL = ToolTieredInfo(
		arrayOf(Material.WOODEN_SHOVEL, Material.GOLDEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL),
		miningTiers
	)

	val TIER_HELMET = ToolTieredInfo(
		arrayOf(Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET),
		arrayOf(
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.WATER_WORKER),
				EnchantmentPair(Enchantment.PROTECTION_EXPLOSIONS, 1, 4),
				EnchantmentPair(Enchantment.VANISHING_CURSE)
			)),
			ToolEnchantmentTier(
				arrayOf(
					EnchantmentPair(Enchantment.WATER_WORKER),
					EnchantmentPair(Enchantment.THORNS, 1),
					EnchantmentPair(Enchantment.VANISHING_CURSE)
				),
				arrayOf(
					EnchantmentPair(Enchantment.PROTECTION_FIRE, 1, 4),
					EnchantmentPair(Enchantment.PROTECTION_PROJECTILE, 1, 4),
					EnchantmentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 2)
				)
			),
			ToolEnchantmentTier(
				arrayOf(EnchantmentPair(Enchantment.THORNS, 2, 3)),
				arrayOf(
					EnchantmentPair(Enchantment.PROTECTION_PROJECTILE, 4),
					EnchantmentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 3, 4)
				)
			)
		)
	)

	private val plainArmorTiers = arrayOf(
		ToolEnchantmentTier(arrayOf(
			EnchantmentPair(Enchantment.PROTECTION_EXPLOSIONS, 1, 4),
			EnchantmentPair(Enchantment.VANISHING_CURSE)
		)),
		ToolEnchantmentTier(
			arrayOf(
				EnchantmentPair(Enchantment.THORNS, 1),
				EnchantmentPair(Enchantment.VANISHING_CURSE)
			),
			arrayOf(
				EnchantmentPair(Enchantment.PROTECTION_FIRE, 1, 4),
				EnchantmentPair(Enchantment.PROTECTION_PROJECTILE, 1, 4),
				EnchantmentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 2)
			)
		),
		ToolEnchantmentTier(
			arrayOf(EnchantmentPair(Enchantment.THORNS, 2, 3)),
			arrayOf(
				EnchantmentPair(Enchantment.PROTECTION_PROJECTILE, 4),
				EnchantmentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 3, 4)
			)
		)
	)

	val TIER_CHESTPLATE = ToolTieredInfo(
		arrayOf(Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE),
		plainArmorTiers
	)

	val TIER_LEGGINGS = ToolTieredInfo(
		arrayOf(Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
		plainArmorTiers
	)

	val TIER_BOOTS = ToolTieredInfo(
		arrayOf(Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS),
		arrayOf(
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.DEPTH_STRIDER, 1, 3),
				EnchantmentPair(Enchantment.PROTECTION_EXPLOSIONS, 1, 4),
				EnchantmentPair(Enchantment.VANISHING_CURSE)
			)),
			ToolEnchantmentTier(
				arrayOf(
					EnchantmentPair(Enchantment.FROST_WALKER, 1, 2),
					EnchantmentPair(Enchantment.THORNS, 1),
					EnchantmentPair(Enchantment.VANISHING_CURSE),
					EnchantmentPair(Enchantment.PROTECTION_FALL, 1, 2)
				),
				arrayOf(
					EnchantmentPair(Enchantment.PROTECTION_FIRE, 1, 4),
					EnchantmentPair(Enchantment.PROTECTION_PROJECTILE, 1, 4),
					EnchantmentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 2)
				)
			),
			ToolEnchantmentTier(
				arrayOf(
					EnchantmentPair(Enchantment.THORNS, 2, 3),
					EnchantmentPair(Enchantment.PROTECTION_FALL, 3, 4)
				),
				arrayOf(
					EnchantmentPair(Enchantment.PROTECTION_PROJECTILE, 4),
					EnchantmentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 3, 4)
				)
			)
		)
	)

	val TIER_BOW = ToolTieredInfo(
		Material.BOW,
		arrayOf(
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.ARROW_KNOCKBACK, 1),
				EnchantmentPair(Enchantment.ARROW_DAMAGE, 1)
			)),
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.ARROW_KNOCKBACK, 2),
				EnchantmentPair(Enchantment.ARROW_DAMAGE, 1, 2)
			)),
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.ARROW_KNOCKBACK, 2),
				EnchantmentPair(Enchantment.ARROW_DAMAGE, 2)
			))
		)
	)

	val TIER_CROSSBOW = ToolTieredInfo(
		Material.CROSSBOW,
		arrayOf(
			ToolEnchantmentTier(arrayOf(
				EnchantmentPair(Enchantment.QUICK_CHARGE, 1, 2)
			)),
			ToolEnchantmentTier(
				arrayOf(
					EnchantmentPair(Enchantment.QUICK_CHARGE, 2, 3)
				),
				arrayOf(
					EnchantmentPair(Enchantment.MULTISHOT),
					EnchantmentPair(Enchantment.PIERCING, 1, 2)
				)
			),
			ToolEnchantmentTier(
				arrayOf(
					EnchantmentPair(Enchantment.QUICK_CHARGE, 3)
				),
				arrayOf(
					EnchantmentPair(Enchantment.MULTISHOT),
					EnchantmentPair(Enchantment.PIERCING, 3, 4)
				)
			)
		)
	)

	val TIER_ELYTRA = ToolTieredInfo(
		Material.ELYTRA,
		ToolEnchantmentTier(arrayOf(
			EnchantmentPair(Enchantment.DURABILITY, 1, 3),
			EnchantmentPair(Enchantment.VANISHING_CURSE)
		))
	)

	val TIER_TRIDENT = ToolTieredInfo(
		Material.TRIDENT,
		ToolEnchantmentTier(
			arrayOf(
				EnchantmentPair(Enchantment.VANISHING_CURSE)
			),
			arrayOf(
				EnchantmentPair(Enchantment.LOYALTY, 1, 3),
				EnchantmentPair(Enchantment.RIPTIDE, 1, 3)
			)
		)
	)

	/* sets */

	val WEAPON_SET = arrayOf(
		TIER_SWORD,
		TIER_AXE
	)

	val ONLY_TOOL_SET = arrayOf(
		TIER_PICKAXE,
		TIER_SHOVEL
	)

	val TOOL_SET = arrayOf(
		TIER_PICKAXE,
		TIER_AXE,
		TIER_SHOVEL
	)

	val ARMOR_SET = arrayOf(
		TIER_HELMET,
		TIER_CHESTPLATE,
		TIER_LEGGINGS,
		TIER_BOOTS
	)

	val BOW_SET = arrayOf(
		TIER_BOW,
		TIER_CROSSBOW
	)

	/* functionality */

	fun getTieredTool(tieredInfos: Array<ToolTieredInfo>, materialType: Int, tier: Int, guaranteed: Int, enchantChance: Double): ItemStack {
		return getTieredTool(Util.randFromArray(tieredInfos), materialType, tier, guaranteed, enchantChance)
	}

	fun getTieredTool(tieredInfos: Array<ToolTieredInfo>, tier: Int, guaranteed: Int, enchantChance: Double): ItemStack {
		return getTieredTool(Util.randFromArray(tieredInfos), 0, tier, guaranteed, enchantChance)
	}

	fun getTieredTool(tieredInfo: ToolTieredInfo, tier: Int, guaranteed: Int, enchantChance: Double): ItemStack {
		return getTieredTool(tieredInfo, 0, tier, guaranteed, enchantChance)
	}

	fun getTieredTool(tieredInfo: ToolTieredInfo, guaranteed: Int, enchantChance: Double): ItemStack {
		return getTieredTool(tieredInfo, 0, 0, guaranteed, enchantChance)
	}

	private fun getTieredTool(tieredInfo: ToolTieredInfo, materialType: Int, tier: Int, guaranteed: Int, enchantChance: Double): ItemStack {
		val applyEnchant = { meta: ItemMeta, enchantmentPair: EnchantmentPair ->
			meta.addEnchant(enchantmentPair.enchantment, Util.randRange(enchantmentPair.minLevel, enchantmentPair.maxLevel), true)
		}

		val item = ItemStack(tieredInfo.materials[materialType])
		val meta = item.itemMeta

		val enchantMentTier = tieredInfo.tiers[tier]

		var numApplied = 0
		val optionIndex = enchantMentTier.available.size
		val totalEnchants = enchantMentTier.available.size + if (enchantMentTier.option.isEmpty()) 0 else 1

		val willApply = Array(totalEnchants) { false }

		while ((numApplied < guaranteed || Math.random() < enchantChance) && numApplied < totalEnchants) {
			var applyIndex = Util.randRange(0, totalEnchants - 1)

			while (willApply[applyIndex])
				applyIndex = (applyIndex + 1) % totalEnchants

			++numApplied

			applyEnchant(meta,
					if (applyIndex == optionIndex) Util.randFromArray(enchantMentTier.option)
					else enchantMentTier.available[applyIndex]
			)
		}

		item.itemMeta = meta

		return item
	}
}
