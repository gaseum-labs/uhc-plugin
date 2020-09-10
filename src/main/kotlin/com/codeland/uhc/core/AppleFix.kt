package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.Util
import com.codeland.uhc.util.Util.binarySearch
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

object AppleFix {
	class Range(var prettyName: String, var countMeta: String, var indexMeta: String, var range: Int, var getDrop: (Material) -> ItemStack)

	val ranges = arrayOf(
		Range("apple", "appleCount", "appleIndex", 200) { leaves -> ItemStack(Material.APPLE) },
		Range("stick", "stickCount", "stickIndex", 50) { leaves -> ItemStack(Material.STICK, Util.randRange(1, 2)) },
		Range("sapling", "saplingCount", "saplingIndex", 20) { leaves ->
			ItemStack(Util.binaryFind(leaves, leavesInfo) { info -> info.leaves }?.sapling ?: Material.OAK_SAPLING)
		}
	)

	class LeafInfo(var leaves: Material, var sapling: Material)

	private val leavesInfo = arrayOf(
		LeafInfo(Material.OAK_LEAVES, Material.OAK_SAPLING),
		LeafInfo(Material.SPRUCE_LEAVES, Material.SPRUCE_SAPLING),
		LeafInfo(Material.BIRCH_LEAVES, Material.BIRCH_SAPLING),
		LeafInfo(Material.JUNGLE_LEAVES, Material.JUNGLE_SAPLING),
		LeafInfo(Material.ACACIA_LEAVES, Material.ACACIA_SAPLING),
		LeafInfo(Material.DARK_OAK_LEAVES, Material.DARK_OAK_SAPLING)
	)

	init {
		leavesInfo.sortBy { info -> info.leaves }
	}

	fun isLeaves(material: Material): Boolean {
		return binarySearch(material, leavesInfo) { info -> info.leaves}
	}

	fun increaseCount(player: Player, countMeta: String): Int {
		val meta = player.getMetadata(countMeta)

		val previous = if (meta.size == 0) {
			resetCount(player, countMeta)
		} else {
			meta[0].asInt()
		}

		player.setMetadata(countMeta, FixedMetadataValue(UHCPlugin.plugin, previous + 1))

		return previous + 1
	}

	fun getCount(player: Player, countMeta: String): Int {
		val meta = player.getMetadata(countMeta)

		return if (meta.size == 0) {
			resetCount(player, countMeta)
		} else {
			meta[0].asInt()
		}
	}

	fun resetCount(player: Player, countMeta: String): Int {
		player.setMetadata(countMeta, FixedMetadataValue(UHCPlugin.plugin, 0))

		return 0
	}

	fun getIndex(player: Player, indexMeta: String, range: Int): Int {
		val meta = player.getMetadata(indexMeta)

		return if (meta.size == 0) {
			resetIndex(player, indexMeta, range)
		} else {
			meta[0].asInt()
		}
	}

	fun resetIndex(player: Player, indexMeta: String, range: Int): Int {
		val index = Util.randRange(1, range)

		player.setMetadata(indexMeta, FixedMetadataValue(UHCPlugin.plugin, index))

		return index
	}

	fun onBreakLeaves(block: Material, player: Player, onItem: (ItemStack) -> Unit) {
		if (isLeaves(block)) {
			ranges.forEach { range ->
				var count = increaseCount(player, range.countMeta)

				if (count == getIndex(player, range.indexMeta, range.range))
					onItem(range.getDrop(block))

				if (count == range.range) {
					resetIndex(player, range.indexMeta, range.range)
					resetCount(player, range.countMeta)
				}
			}
		}
	}
}