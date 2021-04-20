package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.MelonPlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class MelonFix : ChunkPlacerHolder() {
	companion object {
		val melonPlacer = MelonPlacer(40)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(melonPlacer)
}
