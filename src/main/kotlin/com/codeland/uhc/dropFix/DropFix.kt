package com.codeland.uhc.dropFix

import com.codeland.uhc.UHCPlugin
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import kotlin.random.Random

class DropFix(
	val entityType: EntityType,
	val cycleSize: Int,
	val drops: Array<DropEntry>,
	val rares: Array<RareEntry> = emptyArray(),
) {
	private val META_NAME = "Df_${entityType.name}"

	/**
	 * indices manager for a dropfix
	 */
	internal class DropFixData(val cycleSize: Int, val rareSizes: List<Int>) {
		var count = 0
		var rareCounts = Array(rareSizes.size) { 0 }

		var cycleIndices = Array(cycleSize) { it }
		var rareIndices = Array(rareSizes.size) { 0 }

		fun get(onCycle: (Int) -> Unit, onRare: (Int, Int) -> Unit) {
			/* some dropfixes only have rares */
			if (cycleSize != 0) {
				onCycle(cycleIndices[count % cycleSize])
				if ((count + 1) % cycleSize == 0) cycleIndices.shuffle()
			}

			for (i in rareIndices.indices) {
				if (count % rareSizes[i] == rareIndices[i]) onRare(i, rareCounts[i]++)
				if ((count + 1) % rareSizes[i] == 0) rareIndices[i] = Random.nextInt(rareSizes[i])
			}

			++count
		}

		init {
			cycleIndices.shuffle()
			for (i in rareIndices.indices) rareIndices[i] = Random.nextInt(rareSizes[i])
		}
	}

	/**
	 * @param player the player who the mob is dropping items for
	 * @param onDrop can be called multiple times, contains the drop entry
	 * and the cycle index for the player
	 */
	private fun getDrops(player: Player, onDrop: (DropEntry, Int) -> Unit) {
		/* get the dropfixdata from the player */
		val meta = player.getMetadata(META_NAME)
		val data = if (meta.isEmpty()) {
			val ret = DropFixData(cycleSize, rares.map { it.size })
			player.setMetadata(META_NAME, FixedMetadataValue(UHCPlugin.plugin, ret))
			ret
		} else {
			meta[0].value() as DropFixData
		}

		data.get({ cycle ->
			drops.forEach { entry -> onDrop(entry, cycle) }
		}, { rareIndex, cycle ->
			onDrop(rares[rareIndex].dropEntry, cycle)
		})
	}

	val preserve = listOf(
		*Material.values().filter(Material::isRecord).toTypedArray(),
		Material.CREEPER_HEAD,
		Material.ZOMBIE_HEAD,
		Material.SKELETON_SKULL,
		Material.WITHER_SKELETON_SKULL,
	)

	fun onDeath(entity: Entity, killer: Player?, drops: MutableList<ItemStack>) {
		drops.removeIf { it.type !in preserve }

		val looting = killer?.inventory?.itemInMainHand?.itemMeta?.enchants?.asIterable()?.firstOrNull { enchant ->
			enchant.key == Enchantment.LOOT_BONUS_MOBS
		}?.value ?: 0

		val nearestPlayer = killer ?: entity.world.players.minByOrNull { entity.location.distance(it.location) }

		if (nearestPlayer != null) getDrops(nearestPlayer) { entry, cycle ->
			drops.addAll(entry.onDrop(entity, looting, cycle).filterNotNull().filter { it.amount > 0 })
		}
	}
}
