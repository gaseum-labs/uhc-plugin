package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.event.Axe
import com.codeland.uhc.event.Brew
import com.codeland.uhc.lobbyPvp.LoadoutItems.Companion.AmountOption
import com.codeland.uhc.lobbyPvp.LoadoutItems.Companion.EnchantOption
import com.codeland.uhc.lobbyPvp.LoadoutItems.Companion.ItemOption
import com.codeland.uhc.util.ItemUtil
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

val armorEnchants: Array<ItemOption> = arrayOf(
	EnchantOption(1, Enchantment.PROTECTION_ENVIRONMENTAL, 1),
	EnchantOption(2, Enchantment.PROTECTION_ENVIRONMENTAL, 2),
	EnchantOption(1, Enchantment.PROTECTION_PROJECTILE, 1),
	EnchantOption(2, Enchantment.PROTECTION_PROJECTILE, 2),
	EnchantOption(1, Enchantment.THORNS, 1),
	EnchantOption(2, Enchantment.THORNS, 2),
)

val swordEnchants: Array<ItemOption> = arrayOf(
	EnchantOption(1, Enchantment.DAMAGE_ALL, 1),
	EnchantOption(2, Enchantment.DAMAGE_ALL, 2),
	EnchantOption(4, Enchantment.DAMAGE_ALL, 3),
	EnchantOption(2, Enchantment.FIRE_ASPECT, 1),
	EnchantOption(3, Enchantment.FIRE_ASPECT, 2),
	EnchantOption(1, Enchantment.KNOCKBACK, 1),
	EnchantOption(2, Enchantment.KNOCKBACK, 2),
)

val axeEnchants: Array<ItemOption> = arrayOf(
	EnchantOption(1, Enchantment.DAMAGE_ALL, 1),
	EnchantOption(2, Enchantment.DAMAGE_ALL, 2),
	EnchantOption(4, Enchantment.DAMAGE_ALL, 3)
)

val bowEnchants: Array<ItemOption> = arrayOf(
	EnchantOption(1, Enchantment.ARROW_DAMAGE, 1),
	EnchantOption(2, Enchantment.ARROW_DAMAGE, 2),
	EnchantOption(4, Enchantment.ARROW_DAMAGE, 3)
)

val pickaxeEnchants: Array<ItemOption> = arrayOf(
	EnchantOption(1, Enchantment.DIG_SPEED, 4)
)

fun createSpeed(): ItemStack {
	return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.SPEED, false, true)).create()
}

fun createHealth(): ItemStack {
	return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.INSTANT_HEAL, false, true)).create()
}

fun createDamage(): ItemStack {
	return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.INSTANT_DAMAGE, false, true)).create()
}

val arrowAmounts: Array<ItemOption> = arrayOf(
	AmountOption(1, 16),
	AmountOption(2, 32),
	AmountOption(3, 48),
)

val spectralArrowAmounts: Array<ItemOption> = arrayOf(
	AmountOption(1, 10),
	AmountOption(2, 20),
	AmountOption(3, 30),
)

val enderPearlAmounts: Array<ItemOption> = arrayOf(
	AmountOption(1, 1),
	AmountOption(2, 2),
	AmountOption(3, 3),
	AmountOption(4, 4),
)

val goldenAppleAmounts: Array<ItemOption> = arrayOf(
	AmountOption(1, 1),
	AmountOption(2, 2),
	AmountOption(3, 3),
	AmountOption(4, 4),
)

enum class LoadoutItems(val cost: Int, val enchantOptions: Array<ItemOption>, val createItem: () -> ItemStack) {
	/* DO NOT REORDER THESE, IMPORTANT FOR BACKWARDS COMPATIBILITY */
	IRON_HELMET       (1, armorEnchants, { ItemStack(Material.IRON_HELMET) }),
	IRON_CHESTPLATE   (1, armorEnchants, { ItemStack(Material.IRON_CHESTPLATE) }),
	IRON_LEGGINGS     (1, armorEnchants, { ItemStack(Material.IRON_LEGGINGS) }),
	IRON_BOOTS        (1, armorEnchants, { ItemStack(Material.IRON_BOOTS) }),
	DIAMOND_HELMET    (4, armorEnchants, { ItemStack(Material.DIAMOND_HELMET) }),
	DIAMOND_CHESTPLATE(5, armorEnchants, { ItemStack(Material.DIAMOND_CHESTPLATE) }),
	DIAMOND_LEGGINGS  (4, armorEnchants, { ItemStack(Material.DIAMOND_LEGGINGS) }),
	DIAMOND_BOOTS     (4, armorEnchants, { ItemStack(Material.DIAMOND_BOOTS) }),

	IRON_SWORD        (1, swordEnchants, { ItemStack(Material.IRON_SWORD) }),
	DIAMOND_SWORD     (3, swordEnchants, { ItemStack(Material.DIAMOND_SWORD) }),
	IRON_AXE          (1, axeEnchants,   { Axe.ironAxe() }),
	DIAMOND_AXE       (3, axeEnchants,   { Axe.diamondAxe() }),
	BOW               (2, bowEnchants,   { ItemStack(Material.BOW) }),
	CROSSBOW          (2, emptyArray(),  { ItemUtil.enchantThing(ItemStack(Material.CROSSBOW), Enchantment.PIERCING, 1) }),
	SHIELD            (2, emptyArray(),  { ItemStack(Material.SHIELD) }),
	PICKAXE           (1, pickaxeEnchants,  { ItemUtil.enchantThing(ItemStack(Material.DIAMOND_PICKAXE), Enchantment.DIG_SPEED, 2) }),

	ARROWS            (1, arrowAmounts,  { ItemStack(Material.ARROW, 16) }),
	/* UNUSED */ ARROWS_2_UNUSED (1, emptyArray(), { ItemStack(Material.ARROW) }),
	SPECTRAL_ARROWS   (1, spectralArrowAmounts,  { ItemStack(Material.SPECTRAL_ARROW, 10) }),
	/* UNUSED */ SPECTRAL_ARROWS_UNUSED (1, emptyArray(), { ItemStack(Material.SPECTRAL_ARROW) }),

	WATER_BUCKET      (1, emptyArray(),  { ItemStack(Material.WATER_BUCKET) }),
	LAVA_BUCKET       (1, emptyArray(),  { ItemStack(Material.LAVA_BUCKET) }),

	BLOCKS            (1, emptyArray(),  { ItemStack(Material.OAK_PLANKS, 64) }),
	BLOCKS_2          (1, emptyArray(),  { ItemStack(Material.COBBLESTONE, 64) }),
	ENDER_PEARLS      (1, enderPearlAmounts,  { ItemStack(Material.ENDER_PEARL, 1) }),
	GOLDEN_APPLES     (1, goldenAppleAmounts,  { ItemStack(Material.GOLDEN_APPLE, 1) }),

	SPEED_POTION      (2, emptyArray(),  ::createSpeed),
	SPEED_POTION_2    (2, emptyArray(),  ::createSpeed),
	HEALTH_POTION     (2, emptyArray(),  ::createHealth),
	HEALTH_POTION_2   (2, emptyArray(),  ::createHealth),
	DAMAGE_POTION     (2, emptyArray(),  ::createDamage),
	DAMAGE_POTION_2   (2, emptyArray(),  ::createDamage),

	CROSSBOW_2        (2, emptyArray(),  { ItemUtil.enchantThing(ItemStack(Material.CROSSBOW), Enchantment.PIERCING, 1) });

	companion object {
		abstract class ItemOption(val addCost: Int)

		class EnchantOption(addCost: Int, val enchant: Enchantment, val level: Int) : ItemOption(addCost)

		class AmountOption(addCost: Int, val addAmount: Int): ItemOption(addCost)

		class ReplaceOption(addCost: Int, val create: () -> ItemStack) : ItemOption(addCost)
	}
}
