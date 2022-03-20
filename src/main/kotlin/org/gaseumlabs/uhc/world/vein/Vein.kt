package org.gaseumlabs.uhc.world.vein

import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

data class Vein(
	val originalBlocks: List<BlockData>,
	val blocks: List<Block>,
	val placementTime: Int,
) {
	fun centerBlock(): Block {
		return blocks[blocks.size / 2]
	}
}