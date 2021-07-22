package com.codeland.uhc.world.chunkPlacer.impl.nether

import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace

class LavaStreamPlacer(size: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return chunkReadyPlus(world, chunkX, chunkZ)
	}

	fun itb(boolean: Boolean): Int {
		return if (boolean) 1 else 0
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, 8, 17) { block, x, y, z ->
			val east = block.getRelative(BlockFace.EAST).isPassable
			val west = block.getRelative(BlockFace.WEST).isPassable
			val north = block.getRelative(BlockFace.NORTH).isPassable
			val south = block.getRelative(BlockFace.SOUTH).isPassable
			val up = block.getRelative(BlockFace.UP).isPassable
			val down = block.getRelative(BlockFace.DOWN).isPassable

			val aroundCount = itb(east) + itb(west) + itb(north) + itb(south)
			val vertCount = itb(up) + itb(down)

			if (
				(aroundCount == 1 && vertCount == 0) ||
				(aroundCount == 0 && vertCount == 1)
			) {
				block.setType(Material.LAVA, true)
				true
			} else {
				false
			}
		}
	}
}