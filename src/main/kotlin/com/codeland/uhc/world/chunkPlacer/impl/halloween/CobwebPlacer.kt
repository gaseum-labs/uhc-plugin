package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.*
import org.bukkit.Material.COBWEB
import org.bukkit.block.BlockFace

class CobwebPlacer(size: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return chunkReadyAround(world, chunkX, chunkZ)
	}

	override fun place(chunk: Chunk) {
		randomPositionBool(chunk, 10, 64) { block ->
			(block.type == Material.AIR || block.type == Material.CAVE_AIR) &&
			!block.getRelative(BlockFace.UP).isPassable &&
			block.getRelative(BlockFace.DOWN).isPassable &&
			Util.앝리스트어프(
				2,
				!block.getRelative(BlockFace.EAST).isPassable,
				!block.getRelative(BlockFace.WEST).isPassable,
				!block.getRelative(BlockFace.NORTH).isPassable,
				!block.getRelative(BlockFace.SOUTH).isPassable,
			)
		}?.setType(COBWEB, false)
	}
}
