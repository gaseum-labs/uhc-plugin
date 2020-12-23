package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.OreFix
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World

class ChunkSwapper(size: Int, uniqueSeed: Int) : DelayedChunkPlacer(size, uniqueSeed) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (i in -1..1)
			for (j in -1..1)
				if (!world.isChunkGenerated(chunkX + i, chunkZ + j)) return false

		return true
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		val offChunkX = (Math.random() * 2).toInt() * 2 - 1
		val offChunkZ = (Math.random() * 2).toInt() * 2 - 1

		val otherChunk = chunk.world.getChunkAt((chunk.x + offChunkX), (chunk.z + offChunkZ))

		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 0..255) {
					val block1 = chunk.getBlock(x, y, z)
					val block2 = otherChunk.getBlock(x, y, z)

					val temp = block1.type
					block1.setType(block2.type, false)
					block2.setType(temp, false)
				}
			}
		}
	}
}
