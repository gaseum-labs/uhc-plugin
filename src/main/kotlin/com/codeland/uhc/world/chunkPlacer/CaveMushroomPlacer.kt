package com.codeland.uhc.world.chunkPlacer

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.BlockFace

class CaveMushroomPlacer(size: Int, uniqueSeed: Int, val type: Material) : ChunkPlacer(size, uniqueSeed) {
	override fun onGenerate(chunk: Chunk) {
		randomPosition(chunk, 0, 11, 42) { block, x, y, z ->
			if (canPlaceIn(block.type) && canPlaceOn(block.getRelative(BlockFace.DOWN).type) && block.lightLevel <= 12) {
				block.setType(type, false)
				true
			} else {
				false
			}
		}
	}

	private fun canPlaceIn(type: Material): Boolean {
		return when (type) {
			Material.AIR -> true
			Material.CAVE_AIR -> true
			else -> false
		}
	}

	private fun canPlaceOn(type: Material): Boolean {
		return when (type) {
			Material.STONE -> true
			Material.ANDESITE -> true
			Material.DIORITE -> true
			Material.GRANITE -> true
			Material.DIRT -> true
			Material.GRAVEL -> true
			else -> false
		}
	}
}