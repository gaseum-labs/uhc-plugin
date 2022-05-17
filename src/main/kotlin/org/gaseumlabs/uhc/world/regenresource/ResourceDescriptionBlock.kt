package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block

abstract class ResourceDescriptionBlock(
	initialReleased: Int,
	maxReleased: Int,
	maxCurrent: Int,
	interval: Int,
) : ResourceDescription(
	initialReleased,
	maxReleased,
	maxCurrent,
	interval
) {
	/**
	 * IMPORTANT! THESE SHOULD NEVER OVERLAP BETWEEN ANY OTHER BLOCK RESOURCE DESCRIPTIONS
	 */
	abstract fun isBlock(block: Block): Boolean
}
