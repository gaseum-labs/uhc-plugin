package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.util.ItemUtil
import org.bukkit.Material
import org.bukkit.inventory.PlayerInventory

class Loadout(
	val ids: Array<Int> = Array(LOADOUT_SIZE) { -1 },
	val options: Array<Int> = Array(LOADOUT_SIZE) { -1 }
) {
	companion object {
		const val LOADOUT_SIZE = 9 * 4

		fun genDefault(): Loadout {
			val loadout = Loadout()
			val ids = loadout.ids
			val ops = loadout.options

			/* hotbar */
			ids[0] = LoadoutItems.IRON_AXE.ordinal
			ops[0] = 0

			ids[1] = LoadoutItems.BLOCKS.ordinal
			ids[2] = LoadoutItems.BOW.ordinal
			ops[2] = 0

			ids[3] = LoadoutItems.CROSSBOW.ordinal
			ids[4] = LoadoutItems.LAVA_BUCKET.ordinal
			ids[5] = LoadoutItems.WATER_BUCKET.ordinal
			ids[6] = LoadoutItems.GOLDEN_APPLES.ordinal
			ops[6] = 0

			ids[7] = LoadoutItems.SPEED_POTION.ordinal
			ids[8] = LoadoutItems.HEALTH_POTION.ordinal

			/* inventory and armor/offhand */
			ids[9] = LoadoutItems.IRON_HELMET.ordinal
			ops[9] = 0

			ids[10] = LoadoutItems.IRON_CHESTPLATE.ordinal
			ops[10] = 1

			ids[11] = LoadoutItems.IRON_LEGGINGS.ordinal
			ops[11] = 2

			ids[12] = LoadoutItems.IRON_BOOTS.ordinal
			ops[12] = 3

			ids[13] = LoadoutItems.SHIELD.ordinal
			ids[14] = LoadoutItems.SPECTRAL_ARROWS.ordinal
			ids[15] = LoadoutItems.ARROWS.ordinal
			ops[15] = 0

			ids[16] = LoadoutItems.PICKAXE.ordinal

			return loadout
		}
	}

	fun calculateCost(): Int {
		var cost = 0

		ids.indices.forEach { i ->
			val id = ids[i]

			if (id != -1) {
				val loadoutItem = LoadoutItems.values()[id]

				cost += loadoutItem.cost

				val option = options[i]
				if (option != -1) {
					cost += loadoutItem.enchantOptions[option].addCost
				}
			}
		}

		return cost
	}

	fun fillInventory(inventory: PlayerInventory) {
		ids.indices.forEach { i ->
			val id = ids[i]

			if (id != -1) {
				val loadoutItem = LoadoutItems.values()[id]

				/* create the loadout item */
				val stack = loadoutItem.createItem()

				val optionIndex = options[i]
				if (optionIndex != -1) {
					val option = loadoutItem.enchantOptions[optionIndex]

					when (option) {
						is LoadoutItems.Companion.EnchantOption -> {
							val meta = stack.itemMeta
							meta.addEnchant(option.enchant, option.level, true)
							stack.itemMeta = meta
						}

						is LoadoutItems.Companion.AmountOption ->
							stack.amount += option.addAmount
					}
				}

				inventory.setItem(findArmorSpace(stack.type, i), stack)
			}
		}
	}

	private val armorSpaces = arrayOf(
		arrayOf(Material.IRON_BOOTS, Material.DIAMOND_BOOTS),
		arrayOf(Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS),
		arrayOf(Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE),
		arrayOf(Material.IRON_HELMET, Material.DIAMOND_HELMET)
	)

	private fun findArmorSpace(material: Material, slot: Int): Int {
		if (material == Material.SHIELD) return 40

		for (i in armorSpaces.indices) {
			for (j in armorSpaces[i].indices) {
				if (armorSpaces[i][j] === material) return i + 36
			}
		}

		return slot
	}

	fun validate(): Boolean {
		ids.indices.forEach { i ->
			val id = ids[i]
			if (id < -1 || id >= LoadoutItems.values().size) return false

			if (id != -1) {
				val option = options[i]
				if (option < -1 || option >= LoadoutItems.values()[id].enchantOptions.size) return false
			}
		}

		return true
	}
}
