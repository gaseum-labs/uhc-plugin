package com.codeland.uhc.world.chunkPlacer

import org.bukkit.Chunk

abstract class ImmediateChunkPlacer(size: Int, uniqueSeed: Int) : AbstractChunkPlacer(size, uniqueSeed) {
	override fun onGenerate(chunk: Chunk, seed: Int) {
		if (
			shouldGenerate(chunk.x, chunk.z, seed, uniqueSeed, size)
		)
			place(chunk)
	}
}
