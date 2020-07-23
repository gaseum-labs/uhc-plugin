package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

class AppleFix(type: QuirkType) : Quirk(type) {
	override fun onEnable() {}

	override fun onDisable() {}

	companion object {
		class Range(var prettyName: String, var countMeta: String, var indexMeta: String, var range: Int, var getDrop: (Material) -> ItemStack)

		val ranges = arrayOf(
			Range("apple", "appleCount", "appleIndex", 200) { leaves -> ItemStack(Material.APPLE) },
			Range("stick", "stickCount", "stickIndex", 50) { leaves -> ItemStack(Material.STICK, GameRunner.randRange(1, 2)) },
			Range("sapling", "saplingCount", "saplingIndex", 20) { leaves ->
				ItemStack(binarySearchSapling(leaves, leavesInfo) ?: Material.OAK_SAPLING)
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

		fun isLeaves(block: Material): Boolean {
			return binarySearchSapling(block, leavesInfo) != null
		}

		private fun binarySearchSapling(leaves: Material, array: Array<LeafInfo>): Material? {
			var start = 0
			var end = array.size - 1
			var lookFor = leaves.ordinal

			while (true) {
				var position = (end + start) / 2
				var compare = array[position].leaves.ordinal

				when {
					lookFor == compare -> return array[position].sapling
					end - start == 1 -> return null
					lookFor < compare -> end = position
					lookFor > compare -> start = position
				}
			}
		}

		fun increaseCount(player: Player, countMeta: String): Int {
			val meta = player.getMetadata(countMeta)

			val previous = if (meta.size == 0) {
				resetCount(player, countMeta)
			} else {
				meta[0].asInt()
			}

			player.setMetadata(countMeta, FixedMetadataValue(GameRunner.plugin, previous + 1))

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
			player.setMetadata(countMeta, FixedMetadataValue(GameRunner.plugin, 0))

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
			val index = GameRunner.randRange(1, range)

			player.setMetadata(indexMeta, FixedMetadataValue(GameRunner.plugin, index))

			return index
		}

		fun onbreakLeaves(block: Material, player: Player): ArrayList<ItemStack> {
			var ret = ArrayList<ItemStack>()

			if (isLeaves(block)) {
				ranges.forEach { range ->
					var count = increaseCount(player, range.countMeta)

					if (count == getIndex(player, range.indexMeta, range.range))
						ret.add(range.getDrop(block))

					if (count == range.range) {
						resetIndex(player, range.indexMeta, range.range)
						resetCount(player, range.countMeta)
					}
				}
			}

			return ret
		}
	}
}