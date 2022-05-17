package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block
import org.bukkit.entity.Entity

abstract class ResourceDescriptionEntity(
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
	abstract fun setEntity(block: Block): Entity

	abstract fun isEntity(entity: Entity): Boolean
}
