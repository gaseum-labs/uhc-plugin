package com.codeland.uhc.event

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.inventory.ItemStack

class Loot : Listener {
	@EventHandler
	fun onLootOpen(event: LootGenerateEvent) {
		event.loot.removeIf { it.type === Material.ENCHANTED_GOLDEN_APPLE }

		limitToOne(event.loot, Material.GLISTERING_MELON_SLICE)
	}

	private fun limitToOne(loot: MutableList<ItemStack?>, type: Material) {
		val index = loot.indexOfFirst { it?.type === type }

		if (index != -1) {
			loot[index]?.amount = 1

			for (i in index + 1..loot.lastIndex) {
				if (loot[i]?.type == type) {
					loot[i] = null
				}
			}
		}
	}
}