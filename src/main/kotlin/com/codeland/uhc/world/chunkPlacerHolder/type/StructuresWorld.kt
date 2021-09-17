package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.christmas.SnowPlacer
import com.codeland.uhc.world.chunkPlacer.impl.structure.TowerPlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class StructuresWorld : ChunkPlacerHolder() {
	companion object {
		val towerPlacer = TowerPlacer(30)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(towerPlacer)
}
