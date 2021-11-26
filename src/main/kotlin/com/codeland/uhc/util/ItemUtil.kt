package com.codeland.uhc.util

import com.codeland.uhc.UHCPlugin
import io.papermc.paper.enchantments.EnchantmentRarity
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.EntityCategory
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import kotlin.random.Random

object ItemUtil {
	fun halfDamagedItem(type: Material): ItemStack {
		val ret = ItemStack(type)
		val damageable = ret.itemMeta as Damageable
		damageable.damage = type.maxDurability / 2
		ret.itemMeta = damageable as ItemMeta

		return ret
	}

	fun fireworkEffect(type: FireworkEffect.Type, limitColors: Int = -1): FireworkEffect {
		val builder = FireworkEffect.builder()

		/* toggles */
		builder.flicker(Math.random() < 0.5)
		builder.trail(Math.random() < 0.5)

		/* effect type */
		builder.with(type)

		/* colors */
		val numColors = if (limitColors == -1) Random.nextInt(1, 9) else limitColors
		for (i in 0 until numColors) {
			builder.withColor(Color.fromRGB(Random.nextInt(0, 0x1000000)))
		}

		return builder.build()
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
