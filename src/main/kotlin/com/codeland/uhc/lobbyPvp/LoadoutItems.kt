package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.core.AxeFix
import com.codeland.uhc.event.Brew
import com.codeland.uhc.lobbyPvp.LoadoutItems.Companion.EnchantOption
import com.codeland.uhc.util.ItemUtil
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

val armorEnchants = arrayOf(
	EnchantOption(1, Enchantment.PROTECTION_ENVIRONMENTAL, 1),
	EnchantOption(2, Enchantment.PROTECTION_ENVIRONMENTAL, 2),
	EnchantOption(1, Enchantment.PROTECTION_ENVIRONMENTAL, 1),
	EnchantOption(2, Enchantment.PROTECTION_ENVIRONMENTAL, 2),
	EnchantOption(2, Enchantment.THORNS, 1),
	EnchantOption(3, Enchantment.THORNS, 2),
)

val swordEnchants = arrayOf(
	EnchantOption(1, Enchantment.DAMAGE_ALL, 1),
	EnchantOption(2, Enchantment.DAMAGE_ALL, 2),
	EnchantOption(4, Enchantment.DAMAGE_ALL, 3),
	EnchantOption(2, Enchantment.FIRE_ASPECT, 1),
)

val axeEnchants = arrayOf(
	EnchantOption(1, Enchantment.DAMAGE_ALL, 1),
	EnchantOption(2, Enchantment.DAMAGE_ALL, 2),
	EnchantOption(4, Enchantment.DAMAGE_ALL, 3)
)

val bowEnchants = arrayOf(
	EnchantOption(1, Enchantment.ARROW_DAMAGE, 1),
	EnchantOption(2, Enchantment.ARROW_DAMAGE, 2),
	EnchantOption(4, Enchantment.ARROW_DAMAGE, 3)
)

fun createSpeed(): ItemStack {
	return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.SPEED, false, true))
}

fun createHealth(): ItemStack {
	return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.INSTANT_HEAL, false, true))
}

fun createDamage(): ItemStack {
	return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.INSTANT_DAMAGE, false, true))
}

enum class LoadoutItems(val cost: Int, val enchantOptions: Array<EnchantOption>, val createItem: () -> ItemStack) {
	/* DO NOT REORDER THESE, IMPORTANT FOR BACKWARDS COMPATIBILITY */
	IRON_HELMET       (1, armorEnchants, { ItemStack(Material.IRON_HELMET) }),
	IRON_CHESTPLATE   (1, armorEnchants, { ItemStack(Material.IRON_CHESTPLATE) }),
	IRON_LEGGINGS     (1, armorEnchants, { ItemStack(Material.IRON_LEGGINGS) }),
	IRON_BOOTS        (1, armorEnchants, { ItemStack(Material.IRON_LEGGINGS) }),
	DIAMOND_HELMET    (4, armorEnchants, { ItemStack(Material.DIAMOND_HELMET) }),
	DIAMOND_CHESTPLATE(5, armorEnchants, { ItemStack(Material.DIAMOND_CHESTPLATE) }),
	DIAMOND_LEGGINGS  (4, armorEnchants, { ItemStack(Material.DIAMOND_LEGGINGS) }),
	DIAMOND_BOOTS     (4, armorEnchants, { ItemStack(Material.DIAMOND_LEGGINGS) }),

	IRON_SWORD        (1, swordEnchants, { ItemStack(Material.IRON_SWORD) }),
	DIAMOND_SWORD     (3, swordEnchants, { ItemStack(Material.DIAMOND_SWORD) }),
	IRON_AXE          (1, axeEnchants,   { AxeFix.ironAxe() }),
	DIAMOND_AXE       (3, axeEnchants,   { AxeFix.diamondAxe() }),
	BOW               (2, bowEnchants,   { ItemStack(Material.BOW) }),
	CROSSBOW          (2, emptyArray(),  { ItemUtil.enchantThing(ItemStack(Material.CROSSBOW), Enchantment.PIERCING, 1) }),
	SHIELD            (1, emptyArray(),  { ItemStack(Material.SHIELD) }),
	PICKAXE           (2, emptyArray(),  { ItemUtil.enchantThing(ItemStack(Material.DIAMOND_PICKAXE), Enchantment.DIG_SPEED, 2) }),

	ARROWS            (1, emptyArray(),  { ItemStack(Material.ARROW, 16) }),
	ARROWS_2          (1, emptyArray(),  { ItemStack(Material.ARROW, 16) }),
	SPECTRAL_ARROWS   (2, emptyArray(),  { ItemStack(Material.SPECTRAL_ARROW, 16) }),
	SPECTRAL_ARROWS_2 (2, emptyArray(),  { ItemStack(Material.SPECTRAL_ARROW, 16) }),

	WATER_BUCKET      (1, emptyArray(),  { ItemStack(Material.WATER_BUCKET) }),
	LAVA_BUCKET       (1, emptyArray(),  { ItemStack(Material.LAVA_BUCKET) }),

	BLOCKS            (1, emptyArray(),  { ItemStack(Material.OAK_PLANKS, 64) }),
	BLOCKS_2          (1, emptyArray(),  { ItemStack(Material.COBBLESTONE, 64) }),
	ENDER_PEARLS      (3, emptyArray(),  { ItemStack(Material.ENDER_PEARL, 3) }),
	GOLDEN_APPLES     (2, emptyArray(),  { ItemStack(Material.GOLDEN_APPLE, 2) }),

	SPEED_POTION      (2, emptyArray(),  ::createSpeed),
	SPEED_POTION_2    (2, emptyArray(),  ::createSpeed),
	HEALTH_POTION     (2, emptyArray(),  ::createHealth),
	HEALTH_POTION_2   (2, emptyArray(),  ::createHealth),
	DAMAGE_POTION     (2, emptyArray(),  ::createDamage),
	DAMAGE_POTION_2   (2, emptyArray(),  ::createDamage);

	companion object {
		class EnchantOption(val addCost: Int, val enchant: Enchantment, val level: Int)
	}
}