package com.codeland.uhc.quirk.quirks.carePackages

import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.util.Util
import net.bytebuddy.implementation.bytecode.assign.primitive.PrimitiveUnboxingDelegate.UnboxingResponsible
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.*
import kotlin.random.Random

object CarePackagesItems {
	private fun armorPiece(type: Material) = ItemCreator.regular(type).enchant(
		when (Random.nextInt(3)) {
			0 -> Pair(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
			1 -> Pair(Enchantment.THORNS, 2)
			else -> Pair(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
		}
	)

	val helmet = armorPiece(DIAMOND_HELMET)
	val chestplate = armorPiece(DIAMOND_CHESTPLATE)
	val leggings = armorPiece(DIAMOND_LEGGINGS)
	val boots = armorPiece(DIAMOND_BOOTS)

	private fun smallArmorPiece(type: Material): ItemCreator {
		return ItemCreator.regular(type).enchant(
			when (Random.nextInt(3)) {
				0 -> Pair(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
				1 -> Pair(Enchantment.PROTECTION_PROJECTILE, 3)
				else -> Pair(Enchantment.PROTECTION_FIRE, 4)
			}
		)
	}

	val smallHelmet = smallArmorPiece(DIAMOND_HELMET)
	val smallChestplate = smallArmorPiece(DIAMOND_CHESTPLATE)
	val smallLeggings = smallArmorPiece(DIAMOND_LEGGINGS)
	val smallBoots = smallArmorPiece(DIAMOND_BOOTS)

	private fun sword(level: Int) = ItemCreator.regular(DIAMOND_SWORD).enchant(
		when (Random.nextInt(4)) {
			0 -> Pair(Enchantment.DAMAGE_ALL, 2)
			1 -> Pair(Enchantment.DAMAGE_ALL, 3)
			2 -> if (level == 0) {
				Pair(Enchantment.LOOT_BONUS_MOBS, 2)
			} else {
				Pair(Enchantment.FIRE_ASPECT, 1)
			}
			else -> if (level == 0) {
				Pair(Enchantment.LOOT_BONUS_MOBS, 3)
			} else {
				Pair(Enchantment.FIRE_ASPECT, 2)
			}
		}
	)

	val sword0 = sword(0)
	val sword1 = sword(1)

	private fun axe(level: Int) = ItemCreator.regular(DIAMOND_AXE).enchant(
		when (Random.nextInt(2)) {
			0 -> Pair(Enchantment.DAMAGE_ALL, 2 + level)
			else -> Pair(Enchantment.DAMAGE_ALL, 3 + level)
		}
	)

	val axe0 = axe(0)
	val axe1 = axe(1)

	val bow = ItemCreator.regular(BOW).enchant(
		when (Random.nextInt(4)) {
			0 -> Pair(Enchantment.ARROW_DAMAGE, 2)
			1 -> Pair(Enchantment.ARROW_DAMAGE, 3)
			2 -> Pair(Enchantment.ARROW_INFINITE, 1)
			else -> Pair(Enchantment.ARROW_FIRE, 1)
		}
	)

	val shredderBow = ItemCreator.regular(BOW).enchant(
		Enchantment.ARROW_DAMAGE, 5
	).customMeta<Damageable> { it.damage = BOW.maxDurability - 50 }
		.name(Util.gradientString("Shredder", TextColor.color(0xf5020f), TextColor.color(0)))

	private fun crossbow(level: Int): ItemCreator {
		return ItemCreator.regular(CROSSBOW)
			.enchant(Enchantment.QUICK_CHARGE, 2 + level)
			.enchant(Enchantment.PIERCING, 3 + level)
	}

	val crossbow0 = crossbow(0)
	val crossbow1 = crossbow(1)

	val supaPickaxe = ItemCreator.regular(DIAMOND_PICKAXE).enchant(
		Pair(Enchantment.DIG_SPEED, 4)
	).enchant(
		Pair(Enchantment.LOOT_BONUS_BLOCKS, 2)
	).enchant(
		Pair(Enchantment.DURABILITY, 3)
	)

	private fun tippedArrow(potionData: PotionData) = ItemCreator.regular(TIPPED_ARROW)
		.amount(6)
		.customMeta<PotionMeta> { meta ->
			meta.basePotionData = potionData
		}

	val poisonTippedArrow = tippedArrow(PotionData(PotionType.POISON, false, false))
	val slownessTippedArrow = tippedArrow(PotionData(PotionType.SLOWNESS, false, true))
	val weaknessTippedArrow = tippedArrow(PotionData(PotionType.WEAKNESS, true, false))

	val enchantedBook = ItemCreator.regular(ENCHANTED_BOOK).customMeta<EnchantmentStorageMeta> { meta ->
		val (enchantment, level) = when (Random.nextInt(7)) {
			0 -> Pair(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
			1 -> Pair(Enchantment.THORNS, 2)
			2 -> Pair(Enchantment.DAMAGE_ALL, 3)
			3 -> Pair(Enchantment.ARROW_FIRE, 1)
			4 -> Pair(Enchantment.FIRE_ASPECT, 2)
			5 -> Pair(Enchantment.ARROW_DAMAGE, 3)
			else -> Pair(Enchantment.ARROW_INFINITE, 1)
		}
		meta.addStoredEnchant(enchantment, level, true)
	}
	
	val elytraRocket = ItemCreator.regular(FIREWORK_ROCKET).customMeta<FireworkMeta> { meta ->
		meta.power = 3
	}.amount(12)

	val regenerationStew = ItemCreator.regular(SUSPICIOUS_STEW).customMeta<SuspiciousStewMeta> { meta ->
		meta.addCustomEffect(PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 0), true)
	}

	val trident = ItemCreator.regular(TRIDENT).enchant(Enchantment.LOYALTY, 6)
		.name(Util.gradientString("Faithful Fork", TextColor.color(0x2dc26b), TextColor.color(0x2dbdc2)))
}
