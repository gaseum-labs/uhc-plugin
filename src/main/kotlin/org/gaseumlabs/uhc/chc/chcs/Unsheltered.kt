package org.gaseumlabs.uhc.chc.chcs

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.gaseumlabs.uhc.chc.CHC
import org.gaseumlabs.uhc.chc.CHCType
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.Util

class Unsheltered(type: CHCType, game: Game) : CHC<Nothing?>(type, game) {
	override fun customDestroy() {}
	override fun defaultData() = null

	override fun eventListener() = object : Listener {
		@EventHandler
		fun onBreakBlock(event: BlockBreakEvent) {
			val brokenBlock = event.block
			if (
				brokenBlock.isSolid &&
				!Util.binarySearch(brokenBlock.type, acceptedBlocks)
			) {
				SchedulerUtil.nextTick {
					brokenBlock.type = Material.BEDROCK
				}
			}
		}

		@EventHandler
		fun onPlaceBlock(event: BlockPlaceEvent) {
			if (!Util.binarySearch(event.block.type, acceptedBlocks)) {
				event.isCancelled = true
			}
		}
	}

	companion object {
		val acceptedBlocks = arrayOf(
			Material.CRAFTING_TABLE,
			Material.FURNACE,
			Material.BREWING_STAND,
			Material.WHEAT_SEEDS,
			Material.BLAST_FURNACE,
			Material.SMOKER,
			Material.WATER,
			Material.LAVA,
			Material.LADDER,
			Material.ENCHANTING_TABLE,
			Material.BOOKSHELF,
			Material.SMITHING_TABLE,
			Material.LOOM,
			Material.ANVIL,
			Material.FLETCHING_TABLE,
			Material.COMPOSTER,
			Material.CHEST,
			Material.BARREL,
			Material.WET_SPONGE,
			Material.TNT
		)
		init { acceptedBlocks.sort() }
	}
}