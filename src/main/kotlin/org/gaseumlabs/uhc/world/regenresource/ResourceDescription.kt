package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.World
import org.bukkit.block.Block
import org.gaseumlabs.uhc.core.phase.PhaseType

abstract class ResourceDescription(
	val released: HashMap<PhaseType, Int>,

	val current: Int,
	val interval: Int,
	val prettyName: String,
) {
	lateinit var regenResource: RegenResource

	abstract fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>?

	override fun toString(): String {
		return prettyName
	}
}
