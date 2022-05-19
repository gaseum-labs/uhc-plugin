package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.gaseumlabs.uhc.core.phase.PhaseType

abstract class ResourceDescriptionEntity(
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
	abstract fun setEntity(block: Block): Entity

	abstract fun isEntity(entity: Entity): Boolean
}
