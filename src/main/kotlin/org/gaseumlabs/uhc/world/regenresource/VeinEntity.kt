package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.entity.Entity

class VeinEntity(
	val entity: Entity,
	placementTime: Int,
) : Vein(placementTime) {
	fun isLoaded(): Boolean {
		return entity.isValid && entity.isTicking && !entity.isDead
	}
}