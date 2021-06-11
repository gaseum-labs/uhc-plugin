package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.core.AxeFix
import com.codeland.uhc.event.Brew
import com.codeland.uhc.lobbyPvp.LoadoutItems.Companion.EnchantOption
import com.codeland.uhc.util.ItemUtil
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
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
	IRON_BOOTS        (1, armorEnchants, { ItemStack(Material.IRON_BOOTS) }),
	DIAMOND_HELMET    (4, armorEnchants, { ItemStack(Material.DIAMOND_HELMET) }),
	DIAMOND_CHESTPLATE(5, armorEnchants, { ItemStack(Material.DIAMOND_CHESTPLATE) }),
	DIAMOND_LEGGINGS  (4, armorEnchants, { ItemStack(Material.DIAMOND_LEGGINGS) }),
	DIAMOND_BOOTS     (4, armorEnchants, { ItemStack(Material.DIAMOND_BOOTS) }),

	IRON_SWORD        (1, swordEnchants, { ItemStack(Material.IRON_SWORD) }),
	DIAMOND_SWORD     (3, swordEnchants, { ItemStack(Material.DIAMOND_SWORD) }),
	IRON_AXE          (1, axeEnchants,   { AxeFix.ironAxe() }),
	DIAMOND_AXE       (3, axeEnchants,   { AxeFix.diamondAxe() }),
	BOW               (2, bowEnchants,   { ItemStack(Material.BOW) }),
	CROSSBOW          (2, emptyArray(),  { ItemUtil.enchantThing(ItemStack(Material.CROSSBOW), Enchantment.PIERCING, 1) }),
	SHIELD            (2, emptyArray(),  { ItemStack(Material.SHIELD) }),
	PICKAXE           (1, emptyArray(),  { ItemUtil.enchantThing(ItemStack(Material.DIAMOND_PICKAXE), Enchantment.DIG_SPEED, 2) }),

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

		fun calculateCost(loadout: Array<Int>): Int {
			return loadout.fold(0) { acc, i ->
				if (i == -1)
					acc
				else
					acc + values()[i].cost
			}
		}

		fun defaultLoadout(): Array<Int> {
			val loadout = Array(Loadouts.LOADOUT_SIZE) { -1 }

			loadout[0] = IRON_AXE.ordinal
			loadout[1] = BLOCKS.ordinal
			loadout[2] = BOW.ordinal
			loadout[3] = CROSSBOW.ordinal
			loadout[4] = LAVA_BUCKET.ordinal
			loadout[5] = WATER_BUCKET.ordinal
			loadout[6] = GOLDEN_APPLES.ordinal
			loadout[7] = SPEED_POTION.ordinal
			loadout[8] = HEALTH_POTION.ordinal

			loadout[9] = IRON_HELMET.ordinal
			loadout[10] = IRON_CHESTPLATE.ordinal
			loadout[11] = IRON_LEGGINGS.ordinal
			loadout[12] = IRON_BOOTS.ordinal
			loadout[13] = SHIELD.ordinal
			loadout[14] = SPECTRAL_ARROWS.ordinal
			loadout[15] = ARROWS.ordinal
			loadout[16] = PICKAXE.ordinal

			return loadout
		}

		private val armorSpaces = arrayOf(
			arrayOf(Material.IRON_BOOTS, Material.DIAMOND_BOOTS),
			arrayOf(Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS),
			arrayOf(Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE),
			arrayOf(Material.IRON_HELMET, Material.DIAMOND_HELMET)
		)

		fun findArmorSpace(material: Material, slot: Int): Int {
			if (material == Material.SHIELD) return 40

			for (i in armorSpaces.indices) {
				for (j in armorSpaces[i].indices) {
					if (armorSpaces[i][j] === material) return i + 36
				}
			}

			return slot
		}

		fun fillInventory(loadout: Array<Int>, inventory: PlayerInventory) {
			loadout.forEachIndexed { slot, id ->
				if (id != -1) {
					val stack = values()[id].createItem()

					inventory.setItem(findArmorSpace(stack.type, slot), stack)
				}
			}
		}

		val MAX_COST = 24

		/* it's fine to reorder these */
		/* determines the order they display by default in the gui */
		val loadoutItems = arrayOf(
			IRON_HELMET,
			IRON_CHESTPLATE,
			IRON_LEGGINGS,
			IRON_BOOTS,
			DIAMOND_HELMET,
			DIAMOND_CHESTPLATE,
			DIAMOND_LEGGINGS,
			DIAMOND_BOOTS,
			IRON_SWORD,
			DIAMOND_SWORD,
			IRON_AXE,
			DIAMOND_AXE,
			BOW,
			CROSSBOW,
			SHIELD,
			PICKAXE,
			ARROWS,
			ARROWS_2,
			SPECTRAL_ARROWS,
			SPECTRAL_ARROWS_2,
			WATER_BUCKET,
			LAVA_BUCKET,
			BLOCKS,
			BLOCKS_2,
			ENDER_PEARLS,
			GOLDEN_APPLES,
			SPEED_POTION,
			SPEED_POTION_2,
			HEALTH_POTION,
			HEALTH_POTION_2,
			DAMAGE_POTION,
			DAMAGE_POTION_2,
		)
	}
}