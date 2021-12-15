package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.BlockFace

class CaveMushroomPlacer(size: Int, val type: Material) : ChunkPlacer(size) {
	override fun place(chunk: Chunk) {
		randomPositionBool(chunk, 6, 42) { block ->
			block.type.isAir &&
			canPlaceOn(block.getRelative(BlockFace.DOWN).type) &&
			block.lightLevel <= 12
		}?.setType(type, false)
	}

	private fun canPlaceOn(type: Material): Boolean {
		return when (type) {
			Material.STONE -> true
			Material.ANDESITE -> true
			Material.DIORITE -> true
			Material.GRANITE -> true
			Material.DIRT -> true
			Material.GRAVEL -> true
			Material.DEEPSLATE -> true
			else -> false
		}
	}
}
