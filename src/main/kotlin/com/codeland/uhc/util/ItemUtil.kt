package com.codeland.uhc.util

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.Util.randFromArray
import io.papermc.paper.enchantments.EnchantmentRarity
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.EntityCategory
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

object ItemUtil {
	fun randomEnchantedBook(): ItemStack {
		val enchant = Enchantment.values()[Util.randRange(0, Enchantment.values().size - 1)]
		return enchantedBook(enchant, Util.randRange(1, enchant.maxLevel))
	}

	fun enchantedBook(enchant: Enchantment, level: Int): ItemStack {
		val book = ItemStack(Material.ENCHANTED_BOOK)

		val meta = book.itemMeta as EnchantmentStorageMeta
		meta.addStoredEnchant(enchant, level, true)
		book.itemMeta = meta

		return book
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
		damageable.damage = Util.randRange(0, type.maxDurability - 1)
		ret.itemMeta = damageable as ItemMeta

		return ret
	}

	fun halfDamagedItem(type: Material): ItemStack {
		val ret = ItemStack(type)
		val damageable = ret.itemMeta as Damageable
		damageable.damage = type.maxDurability / 2
		ret.itemMeta = damageable as ItemMeta

		return ret
	}

	fun namedItem(type: Material, name: String): ItemStack {
		val ret = ItemStack(type)

		val meta = ret.itemMeta
		meta.setDisplayName(name)
		ret.itemMeta = meta

		return ret
	}

	fun enchantThing(item: ItemStack, enchant: Enchantment, level: Int) {
		val meta = item.itemMeta
		meta.addEnchant(enchant, level, true)
		item.itemMeta = meta
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

	fun fireworkEffect(type: FireworkEffect.Type, limitColors: Int = -1): FireworkEffect {
		val builder = FireworkEffect.builder()

		/* toggles */
		builder.flicker(Math.random() < 0.5)
		builder.trail(Math.random() < 0.5)

		/* effect type */
		builder.with(type)

		/* colors */
		var numColors = if (limitColors == -1) Util.randRange(1, 8) else limitColors
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

	val goodPotionTypes = arrayOf(
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

	val goodPotionEffectTypes = arrayOf(
		PotionEffectType.SPEED,
		PotionEffectType.INCREASE_DAMAGE,
		PotionEffectType.JUMP,
		PotionEffectType.REGENERATION,
		PotionEffectType.FIRE_RESISTANCE,
		PotionEffectType.INVISIBILITY,
		PotionEffectType.NIGHT_VISION,
		PotionEffectType.HEAL,
		PotionEffectType.WATER_BREATHING,
		PotionEffectType.SLOW_FALLING
	)

	val badPotionTypes = arrayOf(
		PotionType.SLOWNESS,
		PotionType.INSTANT_DAMAGE,
		PotionType.WEAKNESS,
		PotionType.POISON
	)

	val badPotionEffectTypes = arrayOf(
		PotionEffectType.SLOW,
		PotionEffectType.HARM,
		PotionEffectType.WEAKNESS,
		PotionEffectType.POISON,
		PotionEffectType.WITHER,
		PotionEffectType.SLOW_DIGGING
	)

	fun randomPotionData(type: PotionType): PotionData {
		val extended = type.isExtendable && Math.random() < 0.5
		val upgraded = !extended && type.isUpgradeable && Math.random() < 0.5

		return PotionData(type, extended, upgraded)
	}

	fun randomPotionData(good: Boolean): PotionData {
		return randomPotionData(randFromArray(if (good) goodPotionTypes else badPotionTypes))
	}

	fun randomPotionEffect(good: Boolean, duration: Int, amplifier: Int): PotionEffect {
		var potionEffectType = randFromArray(if (good) goodPotionEffectTypes else badPotionEffectTypes)

		return PotionEffect(potionEffectType, duration, amplifier, false)
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

	fun randomTippedArrow(amount: Int, potionType: PotionType): ItemStack {
		val itemStack = ItemStack(Material.TIPPED_ARROW, amount)

		val meta = itemStack.itemMeta as PotionMeta
		meta.basePotionData = randomPotionData(potionType)
		itemStack.itemMeta = meta

		return itemStack
	}

	val dyes = arrayOf(
		Material.WHITE_DYE,
		Material.LIGHT_GRAY_DYE,
		Material.GRAY_DYE,
		Material.BLACK_DYE,
		Material.BROWN_DYE,
		Material.RED_DYE,
		Material.ORANGE_DYE,
		Material.YELLOW_DYE,
		Material.LIME_DYE,
		Material.GREEN_DYE,
		Material.CYAN_DYE,
		Material.LIGHT_BLUE_DYE,
		Material.BLUE_DYE,
		Material.PURPLE_DYE,
		Material.MAGENTA_DYE,
		Material.PINK_DYE
	)

	fun randomDye(amount: Int): ItemStack {
		return ItemStack(randFromArray(dyes), amount)
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

	val musicDiscList = arrayOf(
		Material.MUSIC_DISC_13,
		Material.MUSIC_DISC_CAT,
		Material.MUSIC_DISC_BLOCKS,
		Material.MUSIC_DISC_CHIRP,
		Material.MUSIC_DISC_FAR,
		Material.MUSIC_DISC_MALL,
		Material.MUSIC_DISC_MELLOHI,
		Material.MUSIC_DISC_STAL,
		Material.MUSIC_DISC_STRAD,
		Material.MUSIC_DISC_WARD,
		Material.MUSIC_DISC_11,
		Material.MUSIC_DISC_WAIT,
		Material.MUSIC_DISC_PIGSTEP
	)

	fun randomMusicDisc(): ItemStack {
		return ItemStack(randFromArray(musicDiscList))
	}

	fun randomStew(): ItemStack {
		val stack = ItemStack(Material.SUSPICIOUS_STEW)
		val meta = stack.itemMeta as SuspiciousStewMeta

		meta.addCustomEffect(randomPotionEffect(Math.random() < 0.5, 10 * 20, 1), true)

		stack.itemMeta = meta
		return stack
	}

	fun randomDyeArmor(armor: ItemStack): ItemStack {
		val meta = armor.itemMeta as LeatherArmorMeta

		meta.setColor(Color.fromRGB(Util.randRange(0, 0xffffff)))

		armor.itemMeta = meta
		return armor
	}

	class FakeEnchantment : Enchantment(NamespacedKey(UHCPlugin.plugin, "fakeEnchantment")) {
		override fun getName() = ""
		override fun getMaxLevel() = 0
		override fun getStartLevel() = 0
		override fun getItemTarget() = EnchantmentTarget.ARMOR
		override fun isTreasure() = false
		override fun isCursed() = false
		override fun conflictsWith(other: Enchantment) = false
		override fun canEnchantItem(item: ItemStack) = true
		override fun displayName(level: Int) = Component.empty()
		override fun isTradeable() = false
		override fun isDiscoverable() = false
		override fun getRarity() = EnchantmentRarity.COMMON
		override fun getDamageIncrease(level: Int, entityCategory: EntityCategory) = 0.0f
		override fun getActiveSlots() = emptySet<EquipmentSlot>()
	}

	val fakeEnchantment = FakeEnchantment()

	fun randomAddInventory(inventory: Inventory, item: ItemStack) {
		var space = (Math.random() * inventory.size).toInt()

		while (inventory.getItem(space) != null) space = (space + 1) % inventory.size

		inventory.setItem(space, item)
	}
}
