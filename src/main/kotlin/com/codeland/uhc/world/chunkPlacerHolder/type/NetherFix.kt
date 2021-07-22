package com.codeland.uhc.world.chunkPlacerHolder.type;

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.nether.BlackstonePlacer
import com.codeland.uhc.world.chunkPlacer.impl.nether.LavaStreamPlacer
import com.codeland.uhc.world.chunkPlacer.impl.nether.MagmaPlacer
import com.codeland.uhc.world.chunkPlacer.impl.nether.WartPlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class NetherFix : ChunkPlacerHolder() {
	companion object {
		val wartPlacer = WartPlacer(3)
		val blackstonePlacer = BlackstonePlacer()
		val magmaPlacer = MagmaPlacer()
		val lavaStreamPlacer = LavaStreamPlacer(2)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(
		wartPlacer,
		blackstonePlacer,
		lavaStreamPlacer
	)
}
