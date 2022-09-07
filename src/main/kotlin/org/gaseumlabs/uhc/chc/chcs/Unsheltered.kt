package org.gaseumlabs.uhc.chc.chcs

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.gaseumlabs.uhc.chc.NoDataCHC
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.Util

class Unsheltered : NoDataCHC() {
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
		fun onBlockDrop(event: BlockDropItemEvent) {
			val cantAdd = event.player.inventory.addItem(*event.items.map { it.itemStack }.toTypedArray())
			cantAdd.forEach { (_, item) -> event.block.world.dropItem(event.player.location, item)  }
			event.isCancelled = true
		}

		@EventHandler
		fun onPlaceBlock(event: BlockPlaceEvent) {
			if (!Util.binarySearch(event.block.type, acceptedBlocks)) {
				event.isCancelled = true
			}
		}
	}

	companion object {
		val acceptedBlocks = Util.sortedArrayOf(
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
			Material.TNT,
			Material.IRON_DOOR,
			Material.OAK_DOOR,
			Material.BIRCH_DOOR,
			Material.SPRUCE_DOOR,
			Material.JUNGLE_DOOR,
			Material.DARK_OAK_DOOR,
			Material.ACACIA_DOOR,
			Material.IRON_TRAPDOOR,
			Material.OAK_TRAPDOOR,
			Material.BIRCH_TRAPDOOR,
			Material.SPRUCE_TRAPDOOR,
			Material.JUNGLE_TRAPDOOR,
			Material.DARK_OAK_TRAPDOOR,
			Material.ACACIA_TRAPDOOR,
		)
	}
}