package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block
import org.gaseumlabs.uhc.core.phase.PhaseType

abstract class ResourceDescriptionBlock(
	released: HashMap<PhaseType, Int>,
	current: Int,
	interval: Int,
	prettyName: String,
) : ResourceDescription(
	released,
	current,
	interval,
	prettyName
) {
	abstract fun setBlock(block: Block, index: Int)

	/**
	 * IMPORTANT! THESE SHOULD NEVER OVERLAP BETWEEN ANY OTHER BLOCK RESOURCE DESCRIPTIONS
	 */
	abstract fun isBlock(block: Block): Boolean
}
