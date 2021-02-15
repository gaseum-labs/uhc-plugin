package com.codeland.uhc.dropFix

import com.codeland.uhc.UHCPlugin
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

class DropFix(val entityType: EntityType, val dropCycle: Array<Array<DropEntry>>, val naturalDeath: Array<DropEntry>) {
	val metaIndexName = "Dfix_${entityType.name}_I"
	val metaCycleName = "Dfix_${entityType.name}_C"

	fun getIndex(player: Player): Int {
		val meta = player.getMetadata(metaIndexName)

		return if (meta.isEmpty()) resetIndex(player)
		else meta[0].asInt()
	}

	fun increaseIndex(player: Player) {
		val meta = player.getMetadata(metaIndexName)

		var index = (if (meta.isEmpty()) resetIndex(player)
		else meta[0].asInt()) + 1

		if (index == dropCycle.size) {
			index = 0
			resetDropCycle(player)
		}

		player.setMetadata(metaIndexName,FixedMetadataValue(UHCPlugin.plugin, index))
	}

	fun resetIndex(player: Player): Int {
		player.setMetadata(metaIndexName, FixedMetadataValue(UHCPlugin.plugin, 0))
		return 0
	}

	fun resetDropCycle(player: Player): Array<Array<DropEntry>> {
		val used = Array(dropCycle.size) { false }

		val shuffledArray = Array(dropCycle.size) {
			var index = (Math.random() * dropCycle.size).toInt()

			while (used[index]) index = (index + 1) % dropCycle.size
			used[index] = true

			dropCycle[index]
		}

		player.setMetadata(metaCycleName, FixedMetadataValue(UHCPlugin.plugin, shuffledArray))

		return shuffledArray
	}

	fun getDrops(player: Player): Array<DropEntry> {
		val meta = player.getMetadata(metaCycleName)

		val playerCycle = if (meta.size == 0) resetDropCycle(player)
		else meta[0].value() as Array<Array<DropEntry>>

		val ret = playerCycle[getIndex(player)]
		increaseIndex(player)

		return ret
	}

	fun onDeath(entity: Entity, killer: Player?, drops: MutableList<ItemStack>): Boolean {
		return if (killer == null) onNaturalDeath(entity, drops) else onKillEntity(entity, killer, drops)
	}

	fun onKillEntity(entity: Entity, killer: Player, drops: MutableList<ItemStack>): Boolean {
		if (entity.type != entityType) return false

		var looting = 0

		killer.inventory.itemInMainHand.enchantments.any { enchantment ->
			if (enchantment.key == Enchantment.LOOT_BONUS_MOBS) {
				looting = enchantment.value
				true
			} else {
				false
			}
		}

		drops.clear()

		getDrops(killer).forEach { entry ->
			val stacks = entry.onDrop(looting, entity)
			stacks.forEach { stack -> if (stack != null && stack.amount > 0) drops.add(stack) }
		}

		return true
	}

	fun onNaturalDeath(entity: Entity, drops: MutableList<ItemStack>): Boolean {
		if (entity.type != entityType) return false

		drops.clear()

		naturalDeath.forEach { entry ->
			val stacks = entry.onDrop(0, entity)
			stacks.forEach { stack -> if (stack != null && stack.amount > 0) drops.add(stack) }
		}

		return true
	}
}
