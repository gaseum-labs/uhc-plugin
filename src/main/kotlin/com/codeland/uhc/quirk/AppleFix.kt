package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

object AppleFix {
	const val META_COUNT = "leavesCount"
	const val META_INDEX = "appleIndex"

	const val LEAVES_TO_BREAK = 200

	val leaves = arrayOf(
		Material.OAK_LEAVES,
		Material.ACACIA_LEAVES,
		Material.SPRUCE_LEAVES,
		Material.ACACIA_LEAVES,
		Material.BIRCH_LEAVES,
		Material.JUNGLE_LEAVES,
		Material.DARK_OAK_LEAVES
	)

	init {
		leaves.sort()
	}

	fun isLeaves(block: Material): Boolean {
		return GameRunner.binarySearch(block, leaves)
	}

	fun increaseLeavesCount(player: Player): Int {
		val meta = player.getMetadata(META_COUNT)

		val previous = if (meta.size == 0) {
			resetLeavesCount(player)
		} else {
			meta[0].asInt()
		}

		player.setMetadata(META_COUNT, FixedMetadataValue(GameRunner.plugin, previous + 1))

		return previous + 1
	}

	fun getLeavesCount(player: Player): Int {
		val meta = player.getMetadata(META_COUNT)

		return if (meta.size == 0) {
			resetLeavesCount(player)
		} else {
			meta[0].asInt()
		}
	}

	fun getAppleIndex(player: Player): Int {
		val meta = player.getMetadata(META_INDEX)

		return if (meta.size == 0) {
			resetAppleIndex(player)
		} else {
			meta[0].asInt()
		}
	}

	fun resetLeavesCount(player: Player): Int {
		player.setMetadata(META_COUNT, FixedMetadataValue(GameRunner.plugin, 0))

		return 0
	}

	fun resetAppleIndex(player: Player): Int {
		val index = GameRunner.randRange(1, LEAVES_TO_BREAK)

		player.setMetadata(META_INDEX, FixedMetadataValue(GameRunner.plugin, index))

		return index
	}

	fun onbreakLeaves(player: Player): Boolean {
		var count = increaseLeavesCount(player)
		var ret = false

		if (count == getAppleIndex(player))
			ret = true

		if (count == LEAVES_TO_BREAK) {
			resetAppleIndex(player)
			resetLeavesCount(player)
		}

		return ret
	}

	fun modifyDrops(player: Player, drops: MutableCollection<ItemStack>) {
		drops.removeIf { drop -> drop.type == Material.APPLE }

		if (AppleFix.onbreakLeaves(player))
			drops.add(ItemStack(Material.APPLE))
	}
}