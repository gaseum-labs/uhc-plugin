package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block

abstract class ResourceDescriptionBlock(
	initialReleased: Int,
	maxReleased: Int,
	maxCurrent: Int,
	interval: Int,
	prettyName: String,
) : ResourceDescription(
	initialReleased,
	maxReleased,
	maxCurrent,
	interval,
	prettyName
) {
	abstract fun setBlock(block: Block, index: Int)

	/**
	 * IMPORTANT! THESE SHOULD NEVER OVERLAP BETWEEN ANY OTHER BLOCK RESOURCE DESCRIPTIONS
	 */
	abstract fun isBlock(block: Block): Boolean
}
