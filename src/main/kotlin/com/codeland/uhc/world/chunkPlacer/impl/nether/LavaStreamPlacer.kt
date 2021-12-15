package com.codeland.uhc.world.chunkPlacer.impl.nether

import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.*
import org.bukkit.block.BlockFace

class LavaStreamPlacer(size: Int) : ChunkPlacer(size) {
	fun itb(boolean: Boolean): Int {
		return if (boolean) 1 else 0
	}

	override fun place(chunk: Chunk) {
		randomPositionBool(chunk, 8, 17) { block ->
			val east = block.getRelative(BlockFace.EAST).isPassable
			val west = block.getRelative(BlockFace.WEST).isPassable
			val north = block.getRelative(BlockFace.NORTH).isPassable
			val south = block.getRelative(BlockFace.SOUTH).isPassable
			val up = block.getRelative(BlockFace.UP).isPassable
			val down = block.getRelative(BlockFace.DOWN).isPassable

			val aroundCount = itb(east) + itb(west) + itb(north) + itb(south)
			val vertCount = itb(up) + itb(down)

			(aroundCount == 1 && vertCount == 0) ||
			(aroundCount == 0 && vertCount == 1)
		}?.setType(Material.LAVA, true)
	}
}