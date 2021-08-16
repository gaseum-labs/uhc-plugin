package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.NetherIndicatorPlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class NetherIndicators : ChunkPlacerHolder() {
	companion object {
		val netherIndicatorPlacer = NetherIndicatorPlacer(1)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(netherIndicatorPlacer)
}
