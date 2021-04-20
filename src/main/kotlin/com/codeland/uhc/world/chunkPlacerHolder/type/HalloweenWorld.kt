package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.halloween.*
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class HalloweenWorld : ChunkPlacerHolder() {
	companion object {
		val deadBushPlacer = DeadBushPlacer(1)
		val pumpkinPlacer = PumpkinPlacer(3)
		val lanternPlacer = LanternPlacer(4)
		val cobwebPlacer = CobwebPlacer(5)
		val bricksPlacer = BricksPlacer(6)
		val bannerPlacer = BannerPlacer(13)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(
		deadBushPlacer,
		pumpkinPlacer,
		lanternPlacer,
		cobwebPlacer,
		bricksPlacer,
		bannerPlacer
	)
}
