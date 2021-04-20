package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.CaveMushroomPlacer
import com.codeland.uhc.world.chunkPlacer.impl.OxeyePlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder
import org.bukkit.Material

class MushroomOxeyeFix : ChunkPlacerHolder() {
	companion object {
		val oxeyePlacer = OxeyePlacer(12)
		val redMushroomPlacer = CaveMushroomPlacer(12, Material.RED_MUSHROOM)
		val brownMushroomPlacer = CaveMushroomPlacer(12, Material.BROWN_MUSHROOM)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(
		oxeyePlacer,
		redMushroomPlacer,
		brownMushroomPlacer
	)
}
