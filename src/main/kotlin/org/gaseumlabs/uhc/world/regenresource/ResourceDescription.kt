package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.World
import org.bukkit.block.Block

abstract class ResourceDescription {
	/**
	 * based on how many of this vein the team has already collected
	 * how much time should wait until the next generation
	 */
	abstract fun nextInterval(collected: Int): Int

	abstract fun maxCurrent(collected: Int): Int

	abstract fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>?

	abstract fun setBlock(block: Block)
}