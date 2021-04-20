package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.christmas.SnowPlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class ChristmasWorld : ChunkPlacerHolder() {
	companion object {
		val snowPlacer = SnowPlacer(1)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(snowPlacer)
}
