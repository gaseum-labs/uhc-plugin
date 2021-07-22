package com.codeland.uhc.world.chunkPlacer.impl.nether

import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

class MagmaPlacer : DelayedChunkPlacer(1) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return chunkReadyPlus(world, chunkX, chunkZ)
	}

	fun border(block: Block): Boolean {
		return block.type === Material.LAVA || block.type === Material.AIR
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		for (y in 7 downTo 5) {
			for (x in 0..15) for (z in 0..15) {
				val block = chunk.getBlock(x, y, z)

				if (
					block.type === Material.BLACKSTONE && (
						border(block.getRelative(BlockFace.UP)) ||
						border(block.getRelative(BlockFace.DOWN)) ||
						border(block.getRelative(BlockFace.NORTH)) ||
						border(block.getRelative(BlockFace.SOUTH)) ||
						border(block.getRelative(BlockFace.EAST)) ||
						border(block.getRelative(BlockFace.WEST))
					)
				) {
					block.setType(Material.MAGMA_BLOCK, false)
				}
			}
		}
	}
}
