package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.ItemMeta

object ItemUtil {
	fun randomEnchantedBook(): ItemStack {
		val ret = ItemStack(Material.ENCHANTED_BOOK)

		val meta = ret.itemMeta

		val enchant = Enchantment.values()[GameRunner.randRange(0, Enchantment.values().size - 1)]
		meta.addEnchant(enchant, GameRunner.randRange(1, enchant.maxLevel), true)

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
		damageable.damage = GameRunner.randRange(0, type.maxDurability.toInt())
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
			meta.addEnchant(enchantment, GameRunner.randRange(enchantment.startLevel, enchantment.maxLevel), true)
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
				meta.addEnchant(enchant, GameRunner.randRange(enchant.startLevel, enchant.maxLevel), true)
			}
		}

		itemStack.itemMeta = meta

		return itemStack
	}

	fun <T>randFromArray(array: Array<T>): T {
		return array[(Math.random() * array.size).toInt()]
	}
}
