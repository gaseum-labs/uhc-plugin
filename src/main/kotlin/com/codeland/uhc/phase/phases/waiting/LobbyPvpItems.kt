package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.event.Brew
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

object LobbyPvpItems {
	private class EnchantOption(val enchantment: Enchantment, val level: Int)

	private val armorEnchants: Array<EnchantOption?> = arrayOf(
		EnchantOption(Enchantment.PROTECTION_PROJECTILE, 1),
		EnchantOption(Enchantment.PROTECTION_ENVIRONMENTAL, 1),
	)

	private val diggingEnchants: Array<EnchantOption?> = arrayOf(
		EnchantOption(Enchantment.DIG_SPEED, 2),
	)

	private val swordEnchants: Array<EnchantOption?> = arrayOf(
		EnchantOption(Enchantment.DAMAGE_ALL, 1),
	)

	private val axeEnchants: Array<EnchantOption?> = arrayOf(
		EnchantOption(Enchantment.DAMAGE_ALL, 1),
	)

	private val bowEnchants: Array<EnchantOption?> = arrayOf(
		EnchantOption(Enchantment.ARROW_DAMAGE, 1),
	)

	private val crossbowEnchants: Array<EnchantOption?> = arrayOf(
		EnchantOption(Enchantment.PIERCING, 1),
	)

	private val noEnchants: Array<EnchantOption?> = arrayOf(
		null
	)

	private fun genTool(diamond: Material, iron: Material, chance: Double, enchants1: Array<EnchantOption?>): ItemStack {
		val itemStack = ItemStack(if (Math.random() < chance) diamond else iron)

		val meta = itemStack.itemMeta

		val enchant1 = Util.randFromArray(enchants1)
		if (enchant1 != null) meta.addEnchant(enchant1.enchantment, enchant1.level, true)

		itemStack.itemMeta = meta

		return itemStack
	}

	fun genHelmet(): ItemStack {
		return genTool(Material.DIAMOND_HELMET, Material.IRON_HELMET, 0.25, armorEnchants)
	}

	fun genChestplate(): ItemStack {
		return genTool(Material.DIAMOND_CHESTPLATE, Material.IRON_CHESTPLATE, 0.25, armorEnchants)
	}

	fun genLeggings(): ItemStack {
		return genTool(Material.DIAMOND_LEGGINGS, Material.IRON_LEGGINGS, 0.25, armorEnchants)
	}

	fun genBoots(): ItemStack {
		return genTool(Material.DIAMOND_BOOTS, Material.IRON_BOOTS, 0.25, armorEnchants)
	}

	fun genPick(): ItemStack {
		return genTool(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, 0.5, diggingEnchants)
	}

	fun genShovel(): ItemStack {
		return genTool(Material.DIAMOND_SHOVEL, Material.IRON_SHOVEL, 0.5, diggingEnchants)
	}

	fun genSword(): ItemStack {
		return genTool(Material.DIAMOND_SWORD, Material.IRON_SWORD, 0.5, swordEnchants)
	}

	fun genAxe(): ItemStack {
		return genTool(Material.DIAMOND_AXE, Material.IRON_AXE, 0.5, axeEnchants)
	}

	fun genBow(): ItemStack {
		return genTool(Material.BOW, Material.BOW, 0.5, bowEnchants)
	}

	fun genCrossbow(): ItemStack {
		return genTool(Material.CROSSBOW, Material.CROSSBOW, 0.5, crossbowEnchants)
	}

	fun genArrows(): ItemStack {
		return ItemStack(Material.ARROW, 32)
	}

	fun genSpectralArrows(): ItemStack {
		return ItemStack(Material.SPECTRAL_ARROW, 32)
	}

	fun genPoisonPotion(): ItemStack {
		val extended = Math.random() < 0.5
		return Brew.externalCreatePotion(Material.SPLASH_POTION, Brew.POISON_INFO, extended, !extended)
	}

	fun genStrengthPotion(): ItemStack {
		return Brew.externalCreatePotion(Material.POTION, Brew.STRENGTH_INFO, true, false)
	}

	fun genRegenPotion(): ItemStack {
		val extended = Math.random() < 0.5
		return Brew.externalCreatePotion(Material.SPLASH_POTION, Brew.REGEN_INFO,  extended, !extended)
	}

	fun genInstantHealthPotion(): ItemStack {
		return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.INSTANT_HEAL, false, true))
	}

	fun genInstantDamagePotion(): ItemStack {
		return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.INSTANT_DAMAGE, false, true))
	}

	fun genWeaknessPotion(): ItemStack {
		return Brew.createDefaultPotion(Material.SPLASH_POTION, PotionData(PotionType.WEAKNESS, true, false))
	}

	fun genSpeedPotion(): ItemStack {
		return Brew.createDefaultPotion(Material.POTION, PotionData(PotionType.SPEED, false, true))
	}

	val bookEnchants = arrayOf(
		Pair(Enchantment.FIRE_ASPECT, 1),
		Pair(Enchantment.KNOCKBACK, 1),
		Pair(Enchantment.THORNS, 1),
		Pair(Enchantment.DIG_SPEED, 2),
	)

	fun genEnchantedBook(): ItemStack {
		val bookEnchant = Util.randFromArray(bookEnchants)
		return ItemUtil.enchantedBook(bookEnchant.first, bookEnchant.second)
	}

	fun genAnvil(): ItemStack {
		return ItemStack(Material.ANVIL)
	}

	val blockOptions = arrayOf(
		Material.COBBLESTONE,
		Material.ANDESITE,
		Material.OAK_PLANKS
	)

	fun genBlocks(): ItemStack {
		return ItemStack(Util.randFromArray(blockOptions), 64)
	}

	val foodOptions = arrayOf(
		Material.COOKED_PORKCHOP,
		Material.COOKED_BEEF
	)

	fun genFood(): ItemStack {
		return ItemStack(Util.randFromArray(foodOptions), 4)
	}

	fun genGapples(): ItemStack {
		return ItemStack(Material.GOLDEN_APPLE, 2)
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
		return ItemStack(Material.OBSIDIAN, 8)
	}
}
