package com.codeland.uhc.world.chunkPlacer

import org.bukkit.Chunk

abstract class ImmediateChunkPlacer(size: Int) : AbstractChunkPlacer(size) {
	override fun onGenerate(chunk: Chunk, uniqueSeed: Long, worldSeed: Long) {
		if (shouldGenerate(chunk.x, chunk.z, uniqueSeed, worldSeed, size)) {
			place(chunk)
		}
	}
}
