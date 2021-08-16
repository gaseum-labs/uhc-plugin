package com.codeland.uhc.dropFix

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.Util
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

class DropFix(val entityType: EntityType, val dropCycle: Array<Array<DropEntry>>, val naturalDeath: Array<DropEntry> = arrayOf(DropEntry.nothing())) {
	private val metaIndexName = "Dfix_${entityType.name}_I"
	private val metaListName = "Dfix_${entityType.name}_L"

	private fun increaseIndex(player: Player): Int {
		val indexMeta = player.getMetadata(metaIndexName)

		val oldIndex = (if (indexMeta.isEmpty())
			resetIndex(player)
		else
			indexMeta[0].asInt()
		)

		val nextIndex = if (oldIndex + 1 >= dropCycle.size) {
			resetListMeta(player); 0
		} else {
			oldIndex + 1
		}

		player.setMetadata(metaIndexName, FixedMetadataValue(UHCPlugin.plugin, nextIndex))

		return oldIndex
	}

	private fun resetIndex(player: Player): Int {
		player.setMetadata(metaIndexName, FixedMetadataValue(UHCPlugin.plugin, 0))
		return 0
	}

	private fun resetListMeta(player: Player): Array<Int> {
		val array = Array(dropCycle.size) { it }
		array.shuffle()

		player.setMetadata(metaListName, FixedMetadataValue(UHCPlugin.plugin, array))

		return array
	}

	private fun getDrops(player: Player): Array<DropEntry> {
		val listMeta = player.getMetadata(metaListName)

		val list = if (listMeta.isEmpty()) {
			resetListMeta(player)
		} else {
			listMeta[0].value() as Array<Int>
		}

		return dropCycle[list[increaseIndex(player) % list.size] % dropCycle.size]
	}

	fun onDeath(entity: Entity, killer: Player?, drops: MutableList<ItemStack>) {
		val looting = killer?.inventory?.itemInMainHand?.itemMeta?.enchants?.asIterable()?.firstOrNull { enchant ->
			enchant.key == Enchantment.LOOT_BONUS_MOBS
		}?.value ?: 0

		drops.clear()

		(if (killer == null) naturalDeath else getDrops(killer)).forEach { entry ->
			drops.addAll(entry.onDrop(entity, looting).filterNotNull().filter { it.amount > 0 })
		}
	}
}
