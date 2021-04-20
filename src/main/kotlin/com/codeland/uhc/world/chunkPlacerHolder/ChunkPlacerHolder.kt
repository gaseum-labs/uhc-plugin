package com.codeland.uhc.world.chunkPlacerHolder

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer

abstract class ChunkPlacerHolder {
	abstract fun list() : Array<AbstractChunkPlacer>
}
