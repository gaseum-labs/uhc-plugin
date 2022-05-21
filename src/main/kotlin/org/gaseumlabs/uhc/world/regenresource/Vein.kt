package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity

abstract class Vein(
	val x: Int,
	val z: Int,
)

class VeinBlock(
	val originalBlocks: List<BlockData>,
	val blocks: List<Block>,
	x: Int,
	z: Int,
) : Vein(x, z) {
	fun centerBlock(): Block {
		return blocks[blocks.size / 2]
	}
}

class VeinEntity(
	val entity: Entity,
	x: Int,
	z: Int,
) : Vein(x, z) {
	fun isLoaded(): Boolean {
		return entity.isValid && entity.isTicking && !entity.isDead
	}
}
