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
	private data class BottlePart(val material: Material, val amounts: Array<Int>)

	private val bottleParts = arrayOf(
		BottlePart(SAND, arrayOf(6, 9)),
		BottlePart(GLASS, arrayOf(3, 6, 9)),
		BottlePart(GLASS_BOTTLE, arrayOf(3, 6))
	)

	fun randomBottlePart(): ItemStack {
		val bottlePart = Util.randFromArray(bottleParts)

		return ItemStack(bottlePart.material, Util.randFromArray(bottlePart.amounts))
	}

	fun randomStewPart(): ItemStack {
		val rand = Math.random()

		return when {
			rand < 0.25 -> regenerationStew()
			rand < 0.5 -> ItemStack(RED_MUSHROOM, 4)
			rand < 0.75 -> ItemStack(BROWN_MUSHROOM, 4)
			else -> ItemStack(OXEYE_DAISY, 4)
		}
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

	private data class EnchantedBook(val enchantment: Enchantment, val level: Int)

	private val enchantedBooks = arrayOf(
		EnchantedBook(Enchantment.DAMAGE_ALL, 1),
		EnchantedBook(Enchantment.THORNS, 1),
		EnchantedBook(Enchantment.PROTECTION_PROJECTILE, 1),
		EnchantedBook(Enchantment.DIG_SPEED, 5),
		EnchantedBook(Enchantment.ARROW_DAMAGE, 1),
		EnchantedBook(Enchantment.KNOCKBACK, 2),
		EnchantedBook(Enchantment.ARROW_KNOCKBACK, 2)
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

	private data class BrewingIngredient(val materials: Array<Material>, val amounts: Array<Array<Int>>)

	private val brewingIngredients = arrayOf(
		BrewingIngredient(arrayOf(MELON, MELON_SLICE, GLISTERING_MELON_SLICE, GHAST_TEAR), arrayOf(arrayOf(1), arrayOf(2), arrayOf(1, 2), arrayOf(2))),
		BrewingIngredient(arrayOf(SUGAR, SPIDER_EYE, BROWN_MUSHROOM, FERMENTED_SPIDER_EYE), arrayOf(arrayOf(2, 4), arrayOf(1, 2), arrayOf(2), arrayOf(1, 2))),
		BrewingIngredient(arrayOf(MAGMA_CREAM), arrayOf(arrayOf(2))),
		BrewingIngredient(arrayOf(BLAZE_POWDER), arrayOf(arrayOf(1)))
	)

	fun randomBrewingIngredient(): ItemStack {
		val brewingIngredient = Util.randFromArray(brewingIngredients)

		val materialIndex = Util.randRange(0, brewingIngredient.materials.lastIndex)

		return ItemStack(brewingIngredient.materials[materialIndex], Util.randFromArray(brewingIngredient.amounts[materialIndex]))
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

	fun flamingLazerSword(): ItemStack {
		val item = ItemStack(STONE_SWORD)

		val meta = item.itemMeta
		meta.setDisplayName("${ChatColor.RED}Flaming ${ChatColor.GOLD}Lazer ${ChatColor.YELLOW}Sword")
		meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true)
		(meta as Damageable).damage = 65
		item.itemMeta = meta

		return item
	}
}