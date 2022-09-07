package org.gaseumlabs.uhc.util.extensions

import org.bukkit.block.Block
import org.gaseumlabs.uhc.util.BlockPos

object BlockExtensions {
	fun Block.samePlace(other: Block): Boolean {
		return this.x == other.x && this.y == other.y && this.z == other.z
	}

	fun Block.samePlace(other: BlockPos): Boolean {
		return this.x == other.x && this.y == other.y && this.z == other.z
	}

	fun Block.toBlockPos() = BlockPos(x, y, z)
}
