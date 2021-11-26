package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.*
import org.bukkit.block.Biome
import kotlin.random.Random

class NetherIndicatorPlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		SchedulerUtil.nextTick {
			val netherChunk =
				Bukkit.getWorld(WorldManager.NETHER_WORLD_NAME)?.getChunkAt(chunk.x, chunk.z) ?: return@nextTick

			for (i in 0..20) {
				val x = Random.nextInt(0, 16)
				val y = Random.nextInt(5, 15)
				val z = Random.nextInt(0, 16)

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
