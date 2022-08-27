package org.gaseumlabs.uhc.event

import org.bukkit.Material
import org.bukkit.Material.SADDLE
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.metadata.FixedMetadataValue
import org.gaseumlabs.uhc.gui.ItemCreator
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
			else -> {}
		}
	}

	companion object {
		private fun enchant(player: Player, enchantment: Enchantment): Int {
			val rod = player.inventory.itemInMainHand
			if (rod.type !== Material.FISHING_ROD) return 0
			return rod.itemMeta.enchants[enchantment] ?: 0
		}

		private data class FishingData(var index: Int, val list: Array<Int>) {
			fun get() = list[index]

			init {
				list.shuffle()
			}

			companion object {
				fun default(): FishingData {
					return FishingData(0, Array(fishEntries[0].size) { it })
				}
			}
		}

		private val metaKey = "_U_Fish"

		private fun getFishIndex(player: Player): Int {
			val fishMeta = player.getMetadata(metaKey)

			return if (fishMeta.isEmpty()) {
				val fishingData = FishingData.default()
				val ret = fishingData.get()

				fishingData.index = 1
				player.setMetadata(metaKey, FixedMetadataValue(org.gaseumlabs.uhc.UHCPlugin.plugin, fishingData))

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

		private val junk = arrayOf(
			Material.NAUTILUS_SHELL,
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

		private val food = arrayOf(
			Material.COD,
			Material.SALMON,
		)

		private val fishingEnchants = arrayOf(
			Pair(Enchantment.LURE, 2),
			Pair(Enchantment.LUCK, 2),
		)

		private val bowEnchants = arrayOf(
			Pair(Enchantment.ARROW_DAMAGE, 1),
			Pair(Enchantment.ARROW_KNOCKBACK, 1),
			Pair(Enchantment.DURABILITY, 1),
		)

		private val bookEnchants = arrayOf(
			Pair(Enchantment.ARROW_DAMAGE, 2),
			Pair(Enchantment.DAMAGE_ALL, 2),
			Pair(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
			Pair(Enchantment.DIG_SPEED, 2),
			Pair(Enchantment.THORNS, 2),
		)

		private fun junkEntry(random: Random): ItemCreator {
			return ItemCreator.regular(junk[random.nextInt(junk.size)])
		}

		private fun saddleEntry(random: Random): ItemCreator {
			return ItemCreator.regular(SADDLE)
		}

		private fun foodEntry(random: Random): ItemCreator {
			return ItemCreator.regular(food[random.nextInt(food.size)])
		}

		private fun sugarCaneEntry(random: Random): ItemCreator {
			return ItemCreator.regular(Material.SUGAR_CANE)
		}

		private fun leatherEntry(random: Random): ItemCreator {
			return ItemCreator.regular(Material.LEATHER)
		}

		private fun stringEntry(random: Random): ItemCreator {
			return ItemCreator.regular(Material.STRING)
		}

		private fun rodEntry(random: Random): ItemCreator {
			val (enchant, level) = fishingEnchants[random.nextInt(fishingEnchants.size)]
			return ItemCreator.regular(Material.FISHING_ROD)
				.enchant(enchant, level)
		}

		private fun bowEntry(random: Random): ItemCreator {
			val (enchant, level) = bowEnchants[random.nextInt(bowEnchants.size)]
			return ItemCreator.regular(Material.BOW)
				.enchant(enchant, level)
		}

		private fun bookEntry(random: Random): ItemCreator {
			val (enchant, level) = bookEnchants[random.nextInt(bookEnchants.size)]
			return ItemCreator.regular(Material.ENCHANTED_BOOK).customMeta<EnchantmentStorageMeta> { meta ->
				meta.addStoredEnchant(enchant, level, true)
			}
		}

		private fun specialEntry(random: Random): ItemCreator {
			return arrayOf(
				::rodEntry,
				::bowEntry,
				::bookEntry,
			)[random.nextInt(3)](random)
		}

		private fun materialEntry(random: Random): ItemCreator {
			return arrayOf(
				::sugarCaneEntry,
				::leatherEntry,
				::stringEntry,
			)[random.nextInt(3)](random)
		}

		private val fishEntries = arrayOf(
			arrayOf(
				::saddleEntry,
				::junkEntry,
				::junkEntry,
				::junkEntry,
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
				::saddleEntry,
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
			),
			arrayOf(
				::saddleEntry,
				::junkEntry,
				::junkEntry,
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
				::saddleEntry,
				::junkEntry,
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
