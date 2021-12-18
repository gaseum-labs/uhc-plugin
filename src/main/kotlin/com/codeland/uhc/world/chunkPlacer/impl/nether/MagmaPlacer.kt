package com.codeland.uhc.world.chunkPlacer.impl.nether

import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

class MagmaPlacer : ChunkPlacer(1, 1) {
	fun border(block: Block): Boolean {
		return block.type === Material.LAVA || block.type === Material.AIR
	}

	override fun place(chunk: Chunk) {
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
