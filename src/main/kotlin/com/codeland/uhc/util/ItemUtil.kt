package com.codeland.uhc.util

import com.codeland.uhc.util.Util.randFromArray
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

		val meta = ret.itemMeta as EnchantmentStorageMeta

		val enchant = Enchantment.values()[Util.randRange(0, Enchantment.values().size - 1)]
		meta.addStoredEnchant(enchant, Util.randRange(1, enchant.maxLevel), true)

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
}
