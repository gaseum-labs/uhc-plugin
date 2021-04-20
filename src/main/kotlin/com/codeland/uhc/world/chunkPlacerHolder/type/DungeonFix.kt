package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.DungeonChestReplacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class DungeonFix : ChunkPlacerHolder() {
	companion object {
		val dungeonChestReplacer = DungeonChestReplacer(1)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(dungeonChestReplacer)
}