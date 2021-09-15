package com.codeland.uhc.dropFix

import com.codeland.uhc.UHCPlugin
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import kotlin.random.Random

class DropFix(
	val entityType: EntityType,
	val dropCycle: Array<Array<DropEntry>>,
	val rares: Array<Pair<Int, DropEntry>> = emptyArray()
) {
	private val META_NAME = "Df_${entityType.name}"

	internal class DropFixData(val size: Int, val rareSizes: List<Int>) {
		var count = 0
		var cycleIndices = Array(size) { it }
		var rareIndices = Array(rareSizes.size) { 0 }

		fun get(onCycle: (Int) -> Unit, onRare: (Int) -> Unit) {
			if (size != 0) {
				onCycle(cycleIndices[count % size])
				if ((count + 1) % size == 0) cycleIndices.shuffle()
			}

			for (i in rareIndices.indices) {
				if (count % rareSizes[i] == rareIndices[i]) onRare(i)
				if ((count + 1) % rareSizes[i] == 0) rareIndices[i] = Random.nextInt(rareSizes[i])
			}
			++count
		}

		init {
			cycleIndices.shuffle()
			for (i in rareIndices.indices) rareIndices[i] = Random.nextInt(rareSizes[i])
		}
	}

	private fun getDrops(player: Player, onDrop: (DropEntry) -> Unit) {
		val meta = player.getMetadata(META_NAME)

		val data = if (meta.isEmpty()) {
			val ret = DropFixData(dropCycle.size, rares.map { it.first })
			player.setMetadata(META_NAME, FixedMetadataValue(UHCPlugin.plugin, ret))
			ret
		} else {
			meta[0].value() as DropFixData
		}

		data.get({ i ->
			dropCycle[i].forEach { onDrop(it) }
		}, { i ->
			onDrop(rares[i].second)
		})
	}

	fun onDeath(entity: Entity, killer: Player?, drops: MutableList<ItemStack>) {
		drops.clear()

		val looting = killer?.inventory?.itemInMainHand?.itemMeta?.enchants?.asIterable()?.firstOrNull { enchant ->
			enchant.key == Enchantment.LOOT_BONUS_MOBS
		}?.value ?: 0

		val nearestPlayer = killer ?: entity.world.players.minByOrNull { entity.location.distance(it.location) }

		if (nearestPlayer != null) getDrops(nearestPlayer) { entry ->
			drops.addAll(entry.onDrop(entity, looting).filterNotNull().filter { it.amount > 0 })
		}
	}
}
