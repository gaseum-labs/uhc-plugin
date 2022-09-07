package org.gaseumlabs.uhc.util

import org.bukkit.World
import org.bukkit.block.Block

data class BlockPos(val x: Int, val y: Int, val z: Int) {
	fun block(world: World) = world.getBlockAt(x, y, z)

	override fun equals(other: Any?): Boolean {
		return other is BlockPos && x == other.x && y == other.y && z == other.z
	}

	override fun hashCode(): Int {
		var result = x
		result = 31 * result + y
		result = 31 * result + z
		return result
	}

	fun samePlace(other: Block): Boolean {
		return this.x == other.x && this.y == other.y && this.z == other.z
	}

	companion object {
		fun bounds(corner0: BlockPos, corner1: BlockPos): Pair<BlockPos, BlockPos> {
			val x0 = Math.min(corner0.x, corner1.x)
			val x1 = Math.max(corner0.x, corner1.x)
			val y0 = Math.min(corner0.y, corner1.y)
			val y1 = Math.max(corner0.y, corner1.y)
			val z0 = Math.min(corner0.z, corner1.z)
			val z1 = Math.max(corner0.z, corner1.z)

			return BlockPos(x0, y0, z0) to BlockPos(x1, y1, z1)
		}
	}
}