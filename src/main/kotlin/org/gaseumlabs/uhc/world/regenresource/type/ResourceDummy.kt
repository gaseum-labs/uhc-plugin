package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.World
import org.bukkit.block.Block
import org.gaseumlabs.uhc.world.regenresource.ResourceDescription

class ResourceDummy : ResourceDescription() {
	override fun nextInterval(collected: Int): Int {
		return 1000000000
	}

	override fun maxCurrent(collected: Int): Int {
		return 1
	}

	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		return null
	}

	override fun setBlock(block: Block) {
		// do nothing
	}
}
