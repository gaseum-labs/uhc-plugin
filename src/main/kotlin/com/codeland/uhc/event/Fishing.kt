package com.codeland.uhc.event

import com.codeland.uhc.gui.ItemCreator
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class Fishing : Listener {
	@EventHandler
	fun onFish(event: PlayerFishEvent) {
		when (event.state) {
			PlayerFishEvent.State.FISHING -> {
				val rod = event.player.inventory.itemInMainHand
				if (rod.type !== Material.FISHING_ROD) return
				val lure = rod.itemMeta.enchants[Enchantment.LURE] ?: 0

				val hook = event.hook

				hook.applyLure = false
				hook.minWaitTime = (15 - lure * 5).coerceAtLeast(0) * 20
				hook.maxWaitTime = (15 - lure * 5).coerceAtLeast(0) * 20
			}
			PlayerFishEvent.State.CAUGHT_FISH -> {
				val item = event.caught as Item? ?: return
				item.itemStack = ItemStack(Material.SUGAR_CANE)
			}
		}
	}

	companion object {
		val fishMeta = "_U_Fi"

		val junk = arrayOf(
			Material.NAUTILUS_SHELL,
			Material.SADDLE,
			Material.NAME_TAG,
			Material.BOWL,
			Material.LEATHER_BOOTS,
			Material.LILY_PAD,
			Material.ROTTEN_FLESH,
			Material.STICK,
			Material.POTION,
			Material.BONE,
			Material.INK_SAC,
			Material.TRIPWIRE_HOOK,
			Material.TROPICAL_FISH,
			Material.PUFFERFISH,
		)

		val food = arrayOf(
			Material.COD,
			Material.SALMON,
		)

		val fishingEnchants = arrayOf(
			Pair(Enchantment.LURE, 1),
			Pair(Enchantment.LURE, 2),
			Pair(Enchantment.LUCK, 1),
			Pair(Enchantment.LUCK, 2),
			Pair(Enchantment.DURABILITY, 1),
			Pair(Enchantment.DURABILITY, 2),
		)

		val bowEnchants = arrayOf(
			Pair(Enchantment.ARROW_DAMAGE, 1),
			Pair(Enchantment.ARROW_KNOCKBACK, 1),
			Pair(Enchantment.DURABILITY, 1),
		)

		val bookEnchants = arrayOf(
			Pair(Enchantment.ARROW_DAMAGE, 1),
			Pair(Enchantment.DAMAGE_ALL, 1),
			Pair(Enchantment.PROTECTION_ENVIRONMENTAL, 1),
			Pair(Enchantment.DIG_SPEED, 1),
			Pair(Enchantment.THORNS, 1),
		)

		fun junkEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(junk[random.nextInt(junk.size)])
		}

		fun foodEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(food[random.nextInt(food.size)])
		}

		fun sugarCaneEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(Material.SUGAR_CANE)
		}

		fun leatherEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(Material.LEATHER)
		}

		fun stringEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(Material.STRING)
		}

		fun rodEntry(random: Random): ItemCreator {
			val (enchant, level) = fishingEnchants[random.nextInt(fishingEnchants.size)]
			return ItemCreator.fromType(Material.FISHING_ROD)
				.enchant(enchant, level)
		}

		fun bowEntry(random: Random): ItemCreator {
			val (enchant, level) = bowEnchants[random.nextInt(fishingEnchants.size)]
			return ItemCreator.fromType(Material.BOW)
				.enchant(enchant, level)
		}

		fun bookEntry(random: Random): ItemCreator {
			val (enchant, level) = bowEnchants[random.nextInt(fishingEnchants.size)]
			return ItemCreator.fromType(Material.ENCHANTED_BOOK)
				.enchant(enchant, level)
		}

		val fishEntries = arrayOf(
			{ random: Random -> ItemCreator.fromType(junk[random.nextInt(junk.size)]) },
			{ random: Random ->  }
		)

	}
}
