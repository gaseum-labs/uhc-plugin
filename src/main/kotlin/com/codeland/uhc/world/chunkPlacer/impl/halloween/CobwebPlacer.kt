package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.*
import org.bukkit.block.BlockFace

class CobwebPlacer(size: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (i in -1..1) {
			for (j in -1..1) {
				if (!world.isChunkGenerated(chunkX + i, chunkZ + j)) return false
			}
		}

		return true
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, 10, 64) { block, x, y, z ->
			if (
				(block.type == Material.AIR || block.type == Material.CAVE_AIR) &&
				!block.getRelative(BlockFace.UP).isPassable &&
				block.getRelative(BlockFace.DOWN).isPassable
			) {
				var cornerCount = 0
				if (!block.getRelative(BlockFace.EAST).isPassable) ++cornerCount
				if (!block.getRelative(BlockFace.WEST).isPassable) ++cornerCount
				if (!block.getRelative(BlockFace.NORTH).isPassable) ++cornerCount
				if (!block.getRelative(BlockFace.SOUTH).isPassable) ++cornerCount

				if (cornerCount >= 2) {
					block.setType(Material.COBWEB, false)
					true

				} else {
					false
				}

			} else {
				false
			}
		}
	}
}
