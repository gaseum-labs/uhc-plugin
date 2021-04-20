package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace

class MelonPlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, 63, 99) { block, x, y, z ->
			val world = block.world

			if (
				block.type == Material.AIR &&
				block.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK
			) {
				block.setType(Material.MELON, false)
				block.getRelative(BlockFace.DOWN).setType(Material.DIRT, false)
				true
			} else {
				false
			}
		}
	}
}
