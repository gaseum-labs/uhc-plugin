package com.codeland.uhc.world.chunkPlacerHolder.type;

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.WartPlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class NetherFix : ChunkPlacerHolder() {
	companion object {
		val wartPlacer = WartPlacer(3)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(wartPlacer)
}
