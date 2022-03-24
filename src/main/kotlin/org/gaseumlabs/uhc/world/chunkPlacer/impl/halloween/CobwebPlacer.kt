package org.gaseumlabs.uhc.world.chunkPlacer.impl.halloween

import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.*
import org.bukkit.Material.COBWEB
import org.bukkit.block.BlockFace

class CobwebPlacer : ChunkPlacer(5, 5) {
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
