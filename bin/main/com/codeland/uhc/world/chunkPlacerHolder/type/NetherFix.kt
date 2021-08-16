package com.codeland.uhc.world.chunkPlacerHolder.type;

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.nether.*
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class NetherFix : ChunkPlacerHolder() {
	companion object {
		val wartPlacer = WartPlacer(3)
		val blackstonePlacer = BlackstonePlacer()
		val magmaPlacer = MagmaPlacer()
		val lavaStreamPlacer = LavaStreamPlacer(2)
		val basaltPlacer = BasaltPlacer(2)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(
		wartPlacer,
		blackstonePlacer,
		lavaStreamPlacer,
		magmaPlacer,
		basaltPlacer
	)
}
