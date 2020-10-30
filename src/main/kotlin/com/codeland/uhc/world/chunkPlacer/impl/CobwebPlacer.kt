package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Axis
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Orientable
import org.bukkit.block.data.type.Lantern

class CobwebPlacer(size: Int, uniqueSeed: Int) : DelayedChunkPlacer(size, uniqueSeed) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (i in -1..1) {
			for (j in -1..1) {
				if (!world.isChunkGenerated(chunkX + i, chunkZ + j)) return false
			}
		}

		return true
	}

	override fun place(chunk: Chunk) {
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
