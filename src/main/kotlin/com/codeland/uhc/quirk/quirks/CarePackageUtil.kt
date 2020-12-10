package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SuspiciousStewMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

object CarePackageUtil {
	private data class ItemPossibilities(val materials: Array<Material>, val amounts: Array<Array<Int>>)

	private fun randomItem(possibilities: ItemPossibilities): ItemStack {
		val materialIndex = Util.randRange(0, possibilities.materials.lastIndex)
		return ItemStack(possibilities.materials[materialIndex], Util.randFromArray(possibilities.amounts[materialIndex]))
	}

	private val bottlePossibilities = ItemPossibilities(arrayOf(SAND, GLASS, GLASS_BOTTLE), arrayOf(arrayOf(6, 9), arrayOf(3, 6, 9), arrayOf(3, 6)))

	fun randomBottlePart(): ItemStack {
		return randomItem(bottlePossibilities)
	}

	private val stewPossibilities = ItemPossibilities(arrayOf(RED_MUSHROOM, BROWN_MUSHROOM, OXEYE_DAISY), arrayOf(arrayOf(3,5), arrayOf(3,5), arrayOf(3,5)))

	fun randomStewPart(): ItemStack {
		return randomItem(stewPossibilities)
	}

	private val ingredientPossibilities = arrayOf(
		ItemPossibilities(arrayOf(GLISTERING_MELON_SLICE, GHAST_TEAR), arrayOf(arrayOf(1, 2), arrayOf(2))),
		ItemPossibilities(arrayOf(SPIDER_EYE, BROWN_MUSHROOM, FERMENTED_SPIDER_EYE), arrayOf(arrayOf(1, 2), arrayOf(2), arrayOf(1, 2))),
		ItemPossibilities(arrayOf(MAGMA_CREAM), arrayOf(arrayOf(2))),
		ItemPossibilities(arrayOf(BLAZE_POWDER), arrayOf(arrayOf(1)))
	)

	fun randomBrewingIngredient(): ItemStack {
		return randomItem(Util.randFromArray(ingredientPossibilities))
	}

	fun regenerationStew(): ItemStack {
		val stew = ItemStack(SUSPICIOUS_STEW)

		val meta = stew.itemMeta as SuspiciousStewMeta
		meta.addCustomEffect(PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 0), true)
		stew.itemMeta = meta

		return stew
	}

	fun randomBucket(): ItemStack {
		return when {
			Math.random() < 0.5 -> ItemStack(LAVA_BUCKET)
			else -> ItemStack(WATER_BUCKET)
		}
	}

	private data class EnchantData(val enchantment: Enchantment, val level: Int)

	private val enchantedBooks = arrayOf(
		EnchantData(Enchantment.DAMAGE_ALL, 1),
		EnchantData(Enchantment.THORNS, 1),
		EnchantData(Enchantment.PROTECTION_PROJECTILE, 1),
		EnchantData(Enchantment.DIG_SPEED, 5),
		EnchantData(Enchantment.ARROW_DAMAGE, 1),
		EnchantData(Enchantment.KNOCKBACK, 2),
		EnchantData(Enchantment.ARROW_KNOCKBACK, 2)
	)

	fun randomEnchantedBook(twoEnchants: Boolean): ItemStack {
		val book = ItemStack(ENCHANTED_BOOK)

		val index0 = Util.randRange(0, enchantedBooks.lastIndex)
		var index1 = Util.randRange(0, enchantedBooks.lastIndex)

		if (index0 == index1) index1 = (index1 + 1) % enchantedBooks.size

		val meta = book.itemMeta as EnchantmentStorageMeta
		meta.addStoredEnchant(enchantedBooks[index0].enchantment, enchantedBooks[index0].level, true)
		if (twoEnchants) meta.addStoredEnchant(enchantedBooks[index1].enchantment, enchantedBooks[index1].level, true)
		book.itemMeta = meta

		return book
	}

	private data class PotionAssociation(val potionType: PotionType, val upgraded: Boolean, val bottleType: Material)

	private val potionAssociations = arrayOf(
		PotionAssociation(PotionType.INSTANT_HEAL, true, SPLASH_POTION),
		PotionAssociation(PotionType.WEAKNESS, false, SPLASH_POTION),
		PotionAssociation(PotionType.INSTANT_DAMAGE, true, SPLASH_POTION),
		PotionAssociation(PotionType.POISON, true, SPLASH_POTION),
		PotionAssociation(PotionType.FIRE_RESISTANCE, false, POTION),
		PotionAssociation(PotionType.STRENGTH, false, POTION)
	)

	fun randomPotion(): ItemStack {
		val potionAssociation = Util.randFromArray(potionAssociations)

		val potion = ItemStack(potionAssociation.bottleType)
		val meta = potion.itemMeta as PotionMeta                      /* extended for not upgraded, upgraded otherwise */
		meta.basePotionData = PotionData(potionAssociation.potionType, !potionAssociation.upgraded, potionAssociation.upgraded)
		potion.itemMeta = meta

		return potion
	}

	private val armorEnchantments = arrayOf(Enchantment.THORNS, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_PROJECTILE, null)
	private val diamondArmor = arrayOf(DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS)
	private val ironArmor = arrayOf(IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS)

	fun randomArmor(diamond: Boolean): ItemStack {
		val item = ItemStack(Util.randFromArray(if (diamond) diamondArmor else ironArmor))

		val enchantment = Util.randFromArray(armorEnchantments)

		val meta = item.itemMeta
		if (enchantment != null) meta.addEnchant(enchantment, 1, true)
		item.itemMeta = meta

		return item
	}

	private val pickEnchants = arrayOf(
		EnchantData(Enchantment.DIG_SPEED, 2),
		EnchantData(Enchantment.LOOT_BONUS_BLOCKS, 1),
		EnchantData(Enchantment.MENDING, 1),
		EnchantData(Enchantment.SILK_TOUCH, 1),
		null
	)

	private val swordEnchants = arrayOf(
		EnchantData(Enchantment.DAMAGE_ALL, 1),
		EnchantData(Enchantment.LOOT_BONUS_MOBS, 1),
		EnchantData(Enchantment.KNOCKBACK, 2),
		null
	)

	private val axeEnchants = arrayOf(
		EnchantData(Enchantment.DAMAGE_ALL, 1),
		EnchantData(Enchantment.DIG_SPEED, 3),
		null
	)

	fun randomPick(diamond: Boolean, twoEnchants: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_PICKAXE else IRON_PICKAXE, pickEnchants, twoEnchants)
	}

	fun randomSword(diamond: Boolean, twoEnchants: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_SWORD else IRON_SWORD, swordEnchants, twoEnchants)
	}

	fun randomAxe(diamond: Boolean, twoEnchants: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_AXE else IRON_AXE, axeEnchants, twoEnchants)
	}

	private fun enchantedTool(tool: Material, enchantList: Array<EnchantData?>, twoEnchants: Boolean): ItemStack {
		val item = ItemStack(tool)

		val meta = item.itemMeta

		if (twoEnchants) {
			val enchantData = Util.randFromArray(enchantList)
			if (enchantData != null) meta.addEnchant(enchantData.enchantment, enchantData.level, false)

		} else {
			val enchantPair = Util.pickTwo(enchantList)
			if (enchantPair.first != null) meta.addEnchant(enchantPair.first!!.enchantment, enchantPair.first!!.level, false)
			if (enchantPair.second != null) meta.addEnchant(enchantPair.second!!.enchantment, enchantPair.second!!.level, false)
		}

		item.itemMeta = meta

		return item
	}

	fun flamingLazerSword(): ItemStack {
		val item = ItemStack(STONE_SWORD)

		val meta = item.itemMeta
		meta.setDisplayName("${ChatColor.RED}Flaming ${ChatColor.GOLD}Lazer ${ChatColor.YELLOW}Sword")
		meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true)
		meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
		(meta as Damageable).damage = 65
		item.itemMeta = meta

		return item
	}

	fun superSwaggyPants(): ItemStack {
		val item = ItemStack(CHAINMAIL_LEGGINGS)

		val meta = item.itemMeta
		meta.setDisplayName("${ChatColor.AQUA}Super ${ChatColor.BLUE}Swaggy ${ChatColor.DARK_PURPLE}Pants")
		meta.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4, true)
		meta.addEnchant(Enchantment.THORNS, 1, true)
		meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
		(meta as Damageable).damage = 112
		item.itemMeta = meta

		return item
	}

	fun pickOne(vararg possibilities: Int): Int {
		return possibilities[(Math.random() * possibilities.size).toInt()]
	}

	private val glowStonePossibilities = ItemPossibilities(arrayOf(GLOWSTONE, GLOWSTONE_DUST), arrayOf(arrayOf(4, 6), arrayOf(12, 18)))

	fun glowstone(): ItemStack {
		return randomItem(glowStonePossibilities)
	}

	val boats = arrayOf(OAK_BOAT, ACACIA_BOAT, JUNGLE_BOAT, SPRUCE_BOAT, DARK_OAK_BOAT, BIRCH_BOAT)

	fun randomBoat(): ItemStack {
		return ItemStack(Util.randFromArray(boats))
	}
}