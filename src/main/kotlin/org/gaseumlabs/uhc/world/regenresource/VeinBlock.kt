package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

class VeinBlock(
	val originalBlocks: List<BlockData>,
	val blocks: List<Block>,
	placementTime: Int,
) : Vein(placementTime) {
	fun centerBlock(): Block {
		return blocks[blocks.size / 2]
	}
}