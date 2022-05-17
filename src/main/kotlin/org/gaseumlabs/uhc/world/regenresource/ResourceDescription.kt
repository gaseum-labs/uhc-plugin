package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.World
import org.bukkit.block.Block

abstract class ResourceDescription(
	val initialReleased: Int,
	val maxReleased: Int,
	val maxCurrent: Int,
	val interval: Int,
) {
	lateinit var regenResource: RegenResource

	abstract fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>?

	abstract fun setBlock(block: Block)
}
