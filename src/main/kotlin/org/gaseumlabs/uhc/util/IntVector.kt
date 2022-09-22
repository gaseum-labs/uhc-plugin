package org.gaseumlabs.uhc.util

import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

data class IntVector(var x: Int, var y: Int, var z: Int) {
	fun add(x: Int, y: Int, z: Int): IntVector {
		return IntVector(this.x + x, this.y + y, this.z + z)
	}

	fun add(v: IntVector): IntVector {
		return IntVector(this.x + v.x, this.y + v.y, this.z + v.z)
	}

	fun mul(s: Int): IntVector {
		return IntVector(this.x * s, this.y * s, this.z * s)
	}

	fun orthogonal0(): IntVector {
		return IntVector(z, x, y)
	}

	fun orthogonal1(): IntVector {
		return IntVector(y, z, x)
	}

	fun block(world: World): Block {
		return world.getBlockAt(x, y, z)
	}

	companion object {
		fun fromBlockFace(blockFace: BlockFace): IntVector {
			return IntVector(blockFace.modX, blockFace.modY, blockFace.modZ)
		}

		fun fromBlock(block: Block): IntVector {
			return IntVector(block.x, block.y, block.z)
		}
	}
}