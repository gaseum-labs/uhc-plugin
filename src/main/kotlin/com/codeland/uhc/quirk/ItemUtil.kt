package com.codeland.uhc.quirk

import com.codeland.uhc.core.Util
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

object ItemUtil {
	fun randomEnchantedBook(): ItemStack {
		val ret = ItemStack(Material.ENCHANTED_BOOK)

		val meta = ret.itemMeta

		val enchant = Enchantment.values()[Util.randRange(0, Enchantment.values().size - 1)]
		meta.addEnchant(enchant, Util.randRange(1, enchant.maxLevel), true)

		ret.itemMeta = meta

		return ret
	}

	fun fireworkStar(amount: Int, color: Color): ItemStack {
		val stack = ItemStack(Material.FIREWORK_STAR, amount)
		val meta = stack.itemMeta as FireworkEffectMeta
		meta.effect = FireworkEffect.builder().withColor(color).build()
		stack.itemMeta = meta

		return stack
	}

	fun randomDamagedItem(type: Material): ItemStack {
		val ret = ItemStack(type)
		val damageable = ret.itemMeta as Damageable
		damageable.damage = Util.randRange(0, type.maxDurability.toInt())
		ret.itemMeta = damageable as ItemMeta

		return ret
	}

	fun namedItem(type: Material, name: String): ItemStack {
		val ret = ItemStack(Material.CARROT)
		val meta = ret.itemMeta
		meta.setDisplayName(name)
		ret.itemMeta = meta

		return ret
	}

	fun addRandomEnchants(itemStack: ItemStack, enchantList: Array<Enchantment>, probability: Double): ItemStack {
		var enchantIndex = (Math.random() * enchantList.size * (1 / probability)).toInt()

		if (enchantIndex < enchantList.size) {
			val enchantment = enchantList[enchantIndex]

			val meta = itemStack.itemMeta
			meta.addEnchant(enchantment, Util.randRange(enchantment.startLevel, enchantment.maxLevel), true)
			itemStack.itemMeta = meta
		}

		return itemStack
	}

	fun addRandomEnchants(itemStack: ItemStack, enchantList: Array<Array<Enchantment>>, probability: Double): ItemStack {
		var meta = itemStack.itemMeta

		enchantList.forEach { enchantOption ->
			val rand = Math.random()

			if (rand < probability) {
				var enchant = randFromArray(enchantOption)
				meta.addEnchant(enchant, Util.randRange(enchant.startLevel, enchant.maxLevel), true)
			}
		}

		itemStack.itemMeta = meta

		return itemStack
	}

	fun randomFireworkEffect(): FireworkEffect {
		val builder = FireworkEffect.builder()

		/* toggles */
		builder.flicker(Math.random() < 0.5)
		builder.trail(Math.random() < 0.5)

		/* effect type */
		builder.with(randFromArray(FireworkEffect.Type.values()))

		/* colors */
		var numColors = Util.randRange(1, 8)
		for (i in 0 until numColors) {
			builder.withColor(Color.fromRGB(Util.randRange(0, 0xffffff)))
		}

		return builder.build()
	}

	fun fireworkEffect(type: FireworkEffect.Type): FireworkEffect {
		val builder = FireworkEffect.builder()

		/* toggles */
		builder.flicker(Math.random() < 0.5)
		builder.trail(Math.random() < 0.5)

		/* effect type */
		builder.with(type)

		/* colors */
		var numColors = Util.randRange(1, 8)
		for (i in 0 until numColors) {
			builder.withColor(Color.fromRGB(Util.randRange(0, 0xffffff)))
		}

		return builder.build()
	}

	fun randomFireworkStar(amount: Int): ItemStack {
		val itemStack = ItemStack(Material.FIREWORK_STAR, amount)

		val meta = itemStack.itemMeta as FireworkEffectMeta
		meta.effect = randomFireworkEffect()
		itemStack.itemMeta = meta

		return itemStack
	}

	fun randomRocket(amount: Int): ItemStack {
		val itemStack = ItemStack(Material.FIREWORK_ROCKET, amount)

		val meta = itemStack.itemMeta as FireworkMeta

		/* how much gunpowder in this firework */
		var power = Util.randRange(1, 3)
		meta.power = power

		/* how many firework stars in this firework */
		var numEffects = Util.randRange(1, 8 - power)
		for (i in 0 until numEffects) {
			meta.addEffect(randomFireworkEffect())
		}

		itemStack.itemMeta = meta

		return itemStack
	}

	var goodEffects = arrayOf(
		PotionType.SPEED,
		PotionType.STRENGTH,
		PotionType.JUMP,
		PotionType.REGEN,
		PotionType.FIRE_RESISTANCE,
		PotionType.INVISIBILITY,
		PotionType.NIGHT_VISION,
		PotionType.INSTANT_HEAL,
		PotionType.WATER_BREATHING,
		PotionType.SLOW_FALLING,
		PotionType.TURTLE_MASTER
	)

	var badEffects = arrayOf(
		PotionType.SLOWNESS,
		PotionType.INSTANT_DAMAGE,
		PotionType.WEAKNESS,
		PotionType.POISON
	)

	fun randomPotionData(good: Boolean): PotionData {
		var potionType = randFromArray(if (good) goodEffects else badEffects)

		val extended = potionType.isExtendable && Math.random() < 0.5
		val upgraded = !extended && potionType.isUpgradeable && Math.random() < 0.5

		return PotionData(potionType, extended, upgraded)
	}

	fun randomPotion(good: Boolean, throwType: Material): ItemStack {
		val itemStack = ItemStack(throwType)

		val meta = itemStack.itemMeta as PotionMeta
		meta.basePotionData = randomPotionData(good)
		itemStack.itemMeta = meta

		return itemStack
	}

	fun randomTippedArrow(amount: Int): ItemStack {
		val itemStack = ItemStack(Material.TIPPED_ARROW, amount)

		val meta = itemStack.itemMeta as PotionMeta
		meta.basePotionData = randomPotionData(false)
		itemStack.itemMeta = meta

		return itemStack
	}

	val shulkerList = arrayOf(
		Material.SHULKER_BOX,
		Material.WHITE_SHULKER_BOX,
		Material.LIGHT_GRAY_SHULKER_BOX,
		Material.GRAY_SHULKER_BOX,
		Material.BLACK_SHULKER_BOX,
		Material.BROWN_SHULKER_BOX,
		Material.RED_SHULKER_BOX,
		Material.ORANGE_SHULKER_BOX,
		Material.YELLOW_SHULKER_BOX,
		Material.LIME_SHULKER_BOX,
		Material.GREEN_SHULKER_BOX,
		Material.CYAN_SHULKER_BOX,
		Material.LIGHT_BLUE_SHULKER_BOX,
		Material.BLUE_SHULKER_BOX,
		Material.PURPLE_SHULKER_BOX,
		Material.MAGENTA_SHULKER_BOX,
		Material.PINK_SHULKER_BOX
	)

	fun randomShulker(amount: Int): ItemStack {
		return ItemStack(randFromArray(shulkerList), amount)
	}

	fun <T>randFromArray(array: Array<T>): T {
		return array[(Math.random() * array.size).toInt()]
	}

	class EnchantMentPair(val enchantment: Enchantment, inMinLevel: Int = 1, inMaxLevel: Int = 1) {
		val minLevel: Int = inMinLevel
		val maxLevel: Int = inMaxLevel
	}

	class ToolTieredInfo(val materials: Array<Material>, val tiers:Array<Array<EnchantMentPair>>)

	class ToolInfo(val materials: Array<Material>, val enchants: Array<Array<Enchantment>>) {
		companion object {
			val WOOD = 0; val LEATHER = 0
			val GOLD = 1
			val STONE = 2; val CHAIN = 2
			val IRON = 3
			val DIAMOND = 4
			val NETHERITE = 5
			val SHELL = 6
		}
	}

	val weapons = arrayOf(
		ToolTieredInfo(arrayOf(Material.WOODEN_SWORD, Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
			arrayOf(
				arrayOf(
					EnchantMentPair(Enchantment.LOOT_BONUS_MOBS, 1, 3),
					EnchantMentPair(Enchantment.SWEEPING_EDGE, 1, 3),
					EnchantMentPair(Enchantment.DAMAGE_ALL, 1, 1),
					EnchantMentPair(Enchantment.VANISHING_CURSE)
				), arrayOf(
					EnchantMentPair(Enchantment.DAMAGE_ALL, 2, 2),
					EnchantMentPair(Enchantment.KNOCKBACK, 1, 2)
				), arrayOf(
					EnchantMentPair(Enchantment.DAMAGE_ALL, 3, 4)
				)
			)
		),
		ToolTieredInfo(arrayOf(Material.WOODEN_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE),
			arrayOf(
				arrayOf(
					EnchantMentPair(Enchantment.LOOT_BONUS_BLOCKS, 1, 3),
					EnchantMentPair(Enchantment.DIG_SPEED, 1, 5),
					EnchantMentPair(Enchantment.DAMAGE_ALL, 1, 1),
					EnchantMentPair(Enchantment.VANISHING_CURSE)
				), arrayOf(
					EnchantMentPair(Enchantment.DAMAGE_ALL, 2, 2),
					EnchantMentPair(Enchantment.SILK_TOUCH)
				), arrayOf(
					EnchantMentPair(Enchantment.DAMAGE_ALL, 3, 4)
				)
			)
		)
	)

	val tools = arrayOf(
		ToolTieredInfo(arrayOf(Material.WOODEN_PICKAXE, Material.GOLDEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE),
			arrayOf(
				arrayOf(
					EnchantMentPair(Enchantment.MENDING),
					EnchantMentPair(Enchantment.DURABILITY, 1, 3),
					EnchantMentPair(Enchantment.VANISHING_CURSE),
					EnchantMentPair(Enchantment.DIG_SPEED, 1, 3)
				), arrayOf(
					EnchantMentPair(Enchantment.SILK_TOUCH),
					EnchantMentPair(Enchantment.LOOT_BONUS_BLOCKS, 1, 3),
					EnchantMentPair(Enchantment.DIG_SPEED, 4, 5)
				), emptyArray()
			)
		),
		ToolTieredInfo(arrayOf(Material.WOODEN_SHOVEL, Material.GOLDEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL),
			arrayOf(
				arrayOf(
					EnchantMentPair(Enchantment.MENDING),
					EnchantMentPair(Enchantment.DURABILITY, 1, 3),
					EnchantMentPair(Enchantment.VANISHING_CURSE),
					EnchantMentPair(Enchantment.DIG_SPEED, 1, 3)
				), arrayOf(
					EnchantMentPair(Enchantment.SILK_TOUCH),
					EnchantMentPair(Enchantment.LOOT_BONUS_BLOCKS, 1, 3),
					EnchantMentPair(Enchantment.DIG_SPEED, 4, 5)
				), emptyArray()
			)
		)
	)

	val armor = arrayOf(
		ToolTieredInfo(arrayOf(Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET),
			arrayOf(
				arrayOf(
					EnchantMentPair(Enchantment.WATER_WORKER),
					EnchantMentPair(Enchantment.PROTECTION_EXPLOSIONS, 1, 4),
					EnchantMentPair(Enchantment.VANISHING_CURSE)
				), arrayOf(
					EnchantMentPair(Enchantment.PROTECTION_FIRE, 1, 4),
					EnchantMentPair(Enchantment.PROTECTION_PROJECTILE, 1, 4),
					EnchantMentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 1),
					EnchantMentPair(Enchantment.THORNS, 1, 1)
				), arrayOf(
					EnchantMentPair(Enchantment.THORNS, 2, 3),
					EnchantMentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2, 4)
				)
			)
		),
		ToolTieredInfo(arrayOf(Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE),
			arrayOf(
				arrayOf(
					EnchantMentPair(Enchantment.PROTECTION_EXPLOSIONS, 1, 4),
					EnchantMentPair(Enchantment.VANISHING_CURSE)
				), arrayOf(
					EnchantMentPair(Enchantment.PROTECTION_FIRE, 1, 4),
					EnchantMentPair(Enchantment.PROTECTION_PROJECTILE, 1, 4),
					EnchantMentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 1),
					EnchantMentPair(Enchantment.THORNS, 1, 1)
				), arrayOf(
					EnchantMentPair(Enchantment.THORNS, 2, 3),
					EnchantMentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2, 4)
				)
			)
		),
		ToolTieredInfo(arrayOf(Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
			arrayOf(
				arrayOf(
					EnchantMentPair(Enchantment.PROTECTION_EXPLOSIONS, 1, 4),
					EnchantMentPair(Enchantment.VANISHING_CURSE)
				), arrayOf(
					EnchantMentPair(Enchantment.PROTECTION_FIRE, 1, 4),
					EnchantMentPair(Enchantment.PROTECTION_PROJECTILE, 1, 4),
					EnchantMentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 1),
					EnchantMentPair(Enchantment.THORNS, 1, 1)
				), arrayOf(
					EnchantMentPair(Enchantment.THORNS, 2, 3),
					EnchantMentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2, 4)
				)
			)
		),
		ToolTieredInfo(arrayOf(Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS),
			arrayOf(
				arrayOf(
					EnchantMentPair(Enchantment.PROTECTION_EXPLOSIONS, 1, 4),
					EnchantMentPair(Enchantment.VANISHING_CURSE),
					EnchantMentPair(Enchantment.DEPTH_STRIDER, 1, 3)
				), arrayOf(
					EnchantMentPair(Enchantment.PROTECTION_FIRE, 1, 4),
					EnchantMentPair(Enchantment.PROTECTION_PROJECTILE, 1, 4),
					EnchantMentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 1, 1),
					EnchantMentPair(Enchantment.THORNS, 1, 1),
					EnchantMentPair(Enchantment.PROTECTION_FALL, 1, 4)
				), arrayOf(
					EnchantMentPair(Enchantment.THORNS, 2, 3),
					EnchantMentPair(Enchantment.PROTECTION_ENVIRONMENTAL, 2, 4)
				)
			)
		)
	)

	val bow = ToolTieredInfo(arrayOf(Material.BOW),
		arrayOf(
			arrayOf(
				EnchantMentPair(Enchantment.ARROW_KNOCKBACK, 1, 1),
				EnchantMentPair(Enchantment.ARROW_DAMAGE, 1, 1)
			), arrayOf(
				EnchantMentPair(Enchantment.ARROW_KNOCKBACK, 2, 2),
				EnchantMentPair(Enchantment.ARROW_DAMAGE, 2, 2)
			), arrayOf(
				EnchantMentPair(Enchantment.ARROW_DAMAGE, 3, 3)
			)
		)
	)

	val crossbow = ToolTieredInfo(arrayOf(Material.CROSSBOW),
		arrayOf(
			arrayOf(
				EnchantMentPair(Enchantment.QUICK_CHARGE, 1, 3)
			), arrayOf(
				EnchantMentPair(Enchantment.MULTISHOT)
			), arrayOf(
				EnchantMentPair(Enchantment.PIERCING, 1, 4)
			)
		)
	)

	val elytra = ToolInfo(arrayOf(Material.ELYTRA), arrayOf(
		arrayOf(Enchantment.MENDING),
		arrayOf(Enchantment.DURABILITY),
		arrayOf(Enchantment.VANISHING_CURSE)
	))

	val trident = ToolInfo(arrayOf(Material.TRIDENT), arrayOf(
		arrayOf(Enchantment.LOYALTY, Enchantment.RIPTIDE),
		arrayOf(Enchantment.VANISHING_CURSE)
	))


	fun aTool(toolInfo: ToolInfo, enchantChance: Double): ItemStack {
		return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[0]), toolInfo.enchants, enchantChance)
	}

	fun aTieredTool(toolTier: ToolTieredInfo, material: Int, tier: Int, enchantChance: Double): ItemStack {
		val toolType = toolTier.materials[material]

		val ret = ItemStack(toolType)
		val meta = ret.itemMeta

		/* guaranteed one enchantment of highest available tier */
		for (tierIndex in tier downTo 0) {
			if (toolTier.tiers[tierIndex].isEmpty()) continue

			val enchantPair = randFromArray(toolTier.tiers[tierIndex])

			meta.addEnchant(enchantPair.enchantment, Util.randRange(enchantPair.minLevel, enchantPair.maxLevel), true)
			break
		}

		var good = true

		/* get more enchantments */
		while (good && Math.random() < enchantChance) {
			val enchantTier = Util.randRange(0, tier)
			val enchantArray = toolTier.tiers[enchantTier]

			val originalIndex = Util.randRange(0, enchantArray.lastIndex)
			var enchantIndex = originalIndex

			while (meta.enchants.containsKey(enchantArray[enchantIndex].enchantment)) {
				++enchantIndex
				enchantIndex %= enchantArray.size

				if (enchantIndex == originalIndex) {
					good = false
					break
				}
			}

			if (good) {
				val enchantPair = enchantArray[enchantIndex]
				meta.addEnchant(enchantPair.enchantment, Util.randRange(enchantPair.minLevel, enchantPair.maxLevel), true)
			}
		}

		ret.itemMeta = meta
		return ret
	}
}
