package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.core.WorldManager
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace

class NetherIndicatorPlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		SchedulerUtil.nextTick {
			val netherChunk = WorldManager.getNetherWorld().getChunkAt(chunk.x, chunk.z)

			for (i in 0..20) {
				val x = Util.randRange(0, 15)
				val y = Util.randRange(5, 14)
				val z = Util.randRange(0, 15)

				val block = chunk.getBlock(x, y, z)

				if (block.type == Material.STONE) {
					block.setType(when (netherChunk.getBlock(x, y, z).biome) {
						Biome.NETHER_WASTES -> Material.NETHERRACK
						Biome.SOUL_SAND_VALLEY -> Material.SOUL_SOIL
						Biome.BASALT_DELTAS -> Material.BASALT
						Biome.CRIMSON_FOREST -> Material.NETHER_WART_BLOCK
						Biome.WARPED_FOREST -> Material.WARPED_WART_BLOCK
						else -> Material.STONE
					}, false)
				}
			}
		}
	}
}
