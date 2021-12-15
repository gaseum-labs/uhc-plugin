package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional

class AmethystPlacer : ChunkPlacer(1) {
	override fun place(chunk: Chunk) {
		for (y in 8..58) for (x in 0..15) for (z in 0..15) {
			val block = chunk.getBlock(x, y, z)

			if (block.type === Material.BUDDING_AMETHYST) {
				makeCluster(block, BlockFace.EAST)
				makeCluster(block, BlockFace.WEST)
				makeCluster(block, BlockFace.NORTH)
				makeCluster(block, BlockFace.SOUTH)
				makeCluster(block, BlockFace.UP)
				makeCluster(block, BlockFace.DOWN)
			}
		}
	}

	fun makeCluster(origin: Block, face: BlockFace) {
		val relative = origin.getRelative(face)

		when (relative.type) {
			Material.AIR,
			Material.CAVE_AIR,
			Material.SMALL_AMETHYST_BUD,
			Material.MEDIUM_AMETHYST_BUD,
			Material.LARGE_AMETHYST_BUD,
			-> {
				relative.setType(Material.AMETHYST_CLUSTER, false)
				val data = relative.blockData as Directional
				data.facing = face
				relative.blockData = data
			}
			else -> {
			}
		}
	}
}
