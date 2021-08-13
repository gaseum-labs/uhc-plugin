package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.gui.ItemCreator
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.metadata.FixedMetadataValue
import kotlin.random.Random

class Fishing : Listener {
	@EventHandler
	fun onFish(event: PlayerFishEvent) {
		when (event.state) {
			PlayerFishEvent.State.FISHING -> {
				val lure = enchant(event.player, Enchantment.LURE).coerceAtMost(3)
				val hook = event.hook

				hook.applyLure = false

				hook.minWaitTime = 15 * 20 - lure * 50
				hook.maxWaitTime = 15 * 20 - lure * 50
			}
			PlayerFishEvent.State.CAUGHT_FISH -> {
				val luck = enchant(event.player, Enchantment.LUCK).coerceAtMost(3)

				val item = event.caught as Item? ?: return
				item.itemStack = fishEntries[luck][getFishIndex(event.player)](Random(event.hashCode())).create()
			}
		}
	}

	companion object {
		fun enchant(player: Player, enchantment: Enchantment): Int {
			val rod = player.inventory.itemInMainHand
			if (rod.type !== Material.FISHING_ROD) return 0
			return rod.itemMeta.enchants[enchantment] ?: 0
		}

		data class FishingData(var index: Int, val list: Array<Int>) {
			fun get() = list[index]

			init { list.shuffle() }

			companion object {
				fun default(): FishingData {
					return FishingData(0, Array(fishEntries[0].size) { it })
				}
			}
		}

		val metaKey = "_U_Fish"

		fun getFishIndex(player: Player): Int {
			val fishMeta = player.getMetadata(metaKey)

			return if (fishMeta.isEmpty()) {
				val fishingData = FishingData.default()
				val ret = fishingData.get()

				fishingData.index = 1
				player.setMetadata(metaKey, FixedMetadataValue(UHCPlugin.plugin, fishingData))

				ret

			} else {
				val fishingData = fishMeta[0].value() as FishingData
				val ret = fishingData.get()

				++fishingData.index
				if (fishingData.index >= fishingData.list.size) {
					fishingData.index = 0
					fishingData.list.shuffle()
				}

				ret
			}
		}

		val junk = arrayOf(
			Material.NAUTILUS_SHELL,
			Material.SADDLE,
			Material.NAME_TAG,
			Material.BOWL,
			Material.LEATHER_BOOTS,
			Material.LILY_PAD,
			Material.ROTTEN_FLESH,
			Material.STICK,
			Material.GLASS_BOTTLE,
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
			Pair(Enchantment.LUCK, 1),
			Pair(Enchantment.DURABILITY, 1),
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
			return ItemCreator.fromType(junk[random.nextInt(junk.size)], false)
		}

		fun foodEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(food[random.nextInt(food.size)], false)
		}

		fun sugarCaneEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(Material.SUGAR_CANE, false)
		}

		fun leatherEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(Material.LEATHER, false)
		}

		fun stringEntry(random: Random): ItemCreator {
			return ItemCreator.fromType(Material.STRING, false)
		}

		fun rodEntry(random: Random): ItemCreator {
			val (enchant, level) = fishingEnchants[random.nextInt(fishingEnchants.size)]
			return ItemCreator.fromType(Material.FISHING_ROD, false)
				.enchant(enchant, level)
		}

		fun bowEntry(random: Random): ItemCreator {
			val (enchant, level) = bowEnchants[random.nextInt(bowEnchants.size)]
			return ItemCreator.fromType(Material.BOW, false)
				.enchant(enchant, level)
		}

		fun bookEntry(random: Random): ItemCreator {
			val (enchant, level) = bookEnchants[random.nextInt(bookEnchants.size)]
			return ItemCreator.fromType(Material.ENCHANTED_BOOK, false)
				.enchant(enchant, level)
		}

		fun specialEntry(random: Random): ItemCreator {
			return arrayOf(
				::rodEntry,
				::bowEntry,
				::bookEntry,
			)[random.nextInt(3)](random)
		}

		fun materialEntry(random: Random): ItemCreator {
			return arrayOf(
				::sugarCaneEntry,
				::leatherEntry,
				::stringEntry,
			)[random.nextInt(3)](random)
		}

		val fishEntries = arrayOf(
			arrayOf(
				::junkEntry,
				::junkEntry,
				::junkEntry,
				::junkEntry,
				::foodEntry,
				::foodEntry,
				::foodEntry,
				::foodEntry,
				::sugarCaneEntry,
				::sugarCaneEntry,
				::sugarCaneEntry,
				::leatherEntry,
				::stringEntry,
				::rodEntry,
				::bowEntry,
				::bookEntry,
			),
			arrayOf(
				::junkEntry,
				::junkEntry,
				::junkEntry,
				::foodEntry,
				::foodEntry,
				::foodEntry,
				::materialEntry,
				::materialEntry,
				::sugarCaneEntry,
				::sugarCaneEntry,
				::sugarCaneEntry,
				::leatherEntry,
				::stringEntry,
				::rodEntry,
				::bowEntry,
				::bookEntry,
			),
			arrayOf(
				::junkEntry,
				::junkEntry,
				::junkEntry,
				::foodEntry,
				::foodEntry,
				::materialEntry,
				::materialEntry,
				::sugarCaneEntry,
				::sugarCaneEntry,
				::sugarCaneEntry,
				::leatherEntry,
				::stringEntry,
				::rodEntry,
				::bowEntry,
				::bookEntry,
				::specialEntry,
			),
			arrayOf(
				::junkEntry,
				::junkEntry,
				::foodEntry,
				::foodEntry,
				::materialEntry,
				::materialEntry,
				::sugarCaneEntry,
				::sugarCaneEntry,
				::sugarCaneEntry,
				::leatherEntry,
				::stringEntry,
				::rodEntry,
				::bowEntry,
				::bookEntry,
				::specialEntry,
				::specialEntry,
			),
		)
	}
}
