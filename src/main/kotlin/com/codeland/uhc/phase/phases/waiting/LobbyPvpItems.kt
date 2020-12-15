package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

object LobbyPvpItems {
	class EnchantOption(val enchantment: Enchantment, val level: Int)

	val armorEnchants1 = arrayOf(
		EnchantOption(Enchantment.PROTECTION_PROJECTILE, 1),
		EnchantOption(Enchantment.PROTECTION_PROJECTILE, 2),
		EnchantOption(Enchantment.PROTECTION_ENVIRONMENTAL, 1),
		EnchantOption(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
		null
	)

	val armorEnchants2 = arrayOf(
		EnchantOption(Enchantment.THORNS, 1),
		null,
		null,
		null
	)

	val diggingEnchants = arrayOf(
		EnchantOption(Enchantment.DIG_SPEED, 1),
		EnchantOption(Enchantment.DIG_SPEED, 2),
		null
	)

	val swordEnchants1 = arrayOf(
		EnchantOption(Enchantment.DAMAGE_ALL, 1),
		EnchantOption(Enchantment.DAMAGE_ALL, 2),
		null
	)

	val swordEnchants2 = arrayOf(
		EnchantOption(Enchantment.FIRE_ASPECT, 1),
		EnchantOption(Enchantment.KNOCKBACK, 1),
		null
	)

	val axeEnchants = arrayOf(
		EnchantOption(Enchantment.DAMAGE_ALL, 1),
		EnchantOption(Enchantment.DAMAGE_ALL, 2),
		null
	)

	val bowEnchants = arrayOf(
		EnchantOption(Enchantment.ARROW_DAMAGE, 1),
		EnchantOption(Enchantment.ARROW_DAMAGE, 2),
		null
	)

	val crossbowEnchants = arrayOf(
		EnchantOption(Enchantment.PIERCING, 1),
		EnchantOption(Enchantment.PIERCING, 1),
		EnchantOption(Enchantment.PIERCING, 1),
		null
	)

	val noEnchants: Array<EnchantOption?> = arrayOf(
		null
	)

	private fun genTool(diamond: Material, iron: Material, chance: Double, enchants1: Array<EnchantOption?>, enchants2: Array<EnchantOption?>): ItemStack {
		val itemStack = ItemStack(if (Math.random() < chance) diamond else iron)

		val meta = itemStack.itemMeta

		val enchant1 = Util.randFromArray(enchants1)
		if (enchant1 != null) meta.addEnchant(enchant1.enchantment, enchant1.level, true)

		val enchant2 = Util.randFromArray(enchants2)
		if (enchant2 != null) meta.addEnchant(enchant2.enchantment, enchant2.level, true)

		itemStack.itemMeta = meta

		return itemStack
	}

	fun genHelmet(): ItemStack {
		return genTool(Material.DIAMOND_HELMET, Material.IRON_HELMET, 0.25, armorEnchants1, armorEnchants2)
	}

	fun genChestplate(): ItemStack {
		return genTool(Material.DIAMOND_CHESTPLATE, Material.IRON_CHESTPLATE, 0.25, armorEnchants1, armorEnchants2)
	}

	fun genLeggings(): ItemStack {
		return genTool(Material.DIAMOND_LEGGINGS, Material.IRON_LEGGINGS, 0.25, armorEnchants1, armorEnchants2)
	}

	fun genBoots(): ItemStack {
		return genTool(Material.DIAMOND_BOOTS, Material.IRON_BOOTS, 0.25, armorEnchants1, armorEnchants2)
	}

	fun genPick(): ItemStack {
		return genTool(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, 0.5, diggingEnchants, noEnchants)
	}

	fun genShovel(): ItemStack {
		return genTool(Material.DIAMOND_SHOVEL, Material.IRON_SHOVEL, 0.5, diggingEnchants, noEnchants)
	}

	fun genSword(): ItemStack {
		return genTool(Material.DIAMOND_SWORD, Material.IRON_SWORD, 0.5, swordEnchants1, swordEnchants2)
	}

	fun genAxe(): ItemStack {
		return genTool(Material.DIAMOND_AXE, Material.IRON_AXE, 0.5, axeEnchants, diggingEnchants)
	}

	fun genBow(): ItemStack {
		return genTool(Material.BOW, Material.BOW, 0.5, bowEnchants, noEnchants)
	}

	fun genCrossbow(): ItemStack {
		return genTool(Material.CROSSBOW, Material.CROSSBOW, 0.5, crossbowEnchants, noEnchants)
	}

	fun genArrows(): ItemStack {
		return ItemStack(Material.ARROW, Util.randRange(16, 32))
	}

	fun genSpectralArrows(): ItemStack {
		return ItemStack(Material.SPECTRAL_ARROW, Util.randRange(16, 32))
	}

	class PotionOption(val potionType: PotionType, val splash: Boolean, val upgraded: Boolean)

	val potionOptions = arrayOf(
		PotionOption(PotionType.STRENGTH, false, false),
		PotionOption(PotionType.INSTANT_HEAL, true, true),
		PotionOption(PotionType.POISON, true, true),
		PotionOption(PotionType.INSTANT_DAMAGE, true, true),
		PotionOption(PotionType.SPEED, false, true)
	)

	fun genPotion(): ItemStack {
		val potionOption = Util.randFromArray(potionOptions)

		val itemStack = ItemStack(if (potionOption.splash) Material.SPLASH_POTION else Material.POTION)

		val meta = itemStack.itemMeta as PotionMeta
		meta.basePotionData = PotionData(potionOption.potionType, !potionOption.upgraded, potionOption.upgraded)
		itemStack.itemMeta = meta

		return itemStack
	}

	val blockOptions = arrayOf(
		Material.COBBLESTONE,
		Material.ANDESITE,
		Material.OAK_PLANKS
	)

	fun genBlocks(): ItemStack {
		return ItemStack(Util.randFromArray(blockOptions), Util.randRange(56, 64))
	}

	val foodOptions = arrayOf(
		Material.COOKED_PORKCHOP,
		Material.COOKED_BEEF
	)

	fun genFood(): ItemStack {
		return ItemStack(Util.randFromArray(foodOptions), Util.randRange(8, 16))
	}

	fun genGapples(): ItemStack {
		return ItemStack(Material.GOLDEN_APPLE, Util.randRange(1, 2))
	}

	fun genShield(): ItemStack {
		return ItemStack(Material.SHIELD)
	}

	class MaterialsOption(val material: Material, val min: Int, val max: Int)

	val materials = arrayOf(
		MaterialsOption(Material.COAL, 3, 7),
		MaterialsOption(Material.LAPIS_LAZULI, 6, 9),
		MaterialsOption(Material.GOLD_INGOT, 7, 9),
		MaterialsOption(Material.STRING, 1, 2),
		MaterialsOption(Material.OBSIDIAN, 2, 4),
		MaterialsOption(Material.DIAMOND, 1, 1),
		MaterialsOption(Material.BLAZE_ROD, 1, 1),
		MaterialsOption(Material.IRON_INGOT, 5, 11),
		MaterialsOption(Material.LEATHER, 1, 4),
		MaterialsOption(Material.PAPER, 3, 6),
		MaterialsOption(Material.NETHER_WART, 1, 2),
		MaterialsOption(Material.APPLE, 1, 1),
	)

	fun genMaterial(): ItemStack {
		val materialOption = Util.randFromArray(materials)

		return ItemStack(materialOption.material, Util.randRange(materialOption.min, materialOption.max))
	}

	fun genWaterBucket(): ItemStack {
		return ItemStack(Material.WATER_BUCKET)
	}

	fun genLavaBucket(): ItemStack {
		return ItemStack(Material.LAVA_BUCKET)
	}

	fun genEndCrystal(): ItemStack {
		return ItemStack(Material.END_CRYSTAL)
	}

	fun genObsidian(): ItemStack {
		return ItemStack(Material.OBSIDIAN, Util.randRange(10, 16))
	}
}
