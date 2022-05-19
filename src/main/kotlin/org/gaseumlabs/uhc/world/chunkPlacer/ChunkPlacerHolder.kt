package org.gaseumlabs.uhc.world.chunkPlacer

import org.gaseumlabs.uhc.world.chunkPlacer.impl.christmas.SnowPlacer
import org.gaseumlabs.uhc.world.chunkPlacer.impl.halloween.*
import org.gaseumlabs.uhc.world.chunkPlacer.impl.structure.TowerPlacer
import org.bukkit.Chunk

enum class ChunkPlacerHolder(val chunkPlacer: ChunkPlacer) {
	/** CHCS */

	/* structures */
	TOWER(TowerPlacer()),

	/* halloween */
	DEAD_BUSH(DeadBushPlacer()),
	PUMPKIN(PumpkinPlacer()),
	LANTERN(LanternPlacer()),
	COBWEB(CobwebPlacer()),
	BRICKS(BricksPlacer()),
	BANNER(BannerPlacer()),

	/* christmas */
	SNOW(SnowPlacer()),
	;

	fun addToList(chunk: Chunk, list: ArrayList<ChunkPlacer>) {
		if (chunkPlacer.shouldGenerate(chunk.x, chunk.z, ordinal.toLong(), chunk.world.seed)) {
			list.add(chunkPlacer)
		}
	}

	operator fun component1(): ChunkPlacer {
		return chunkPlacer
	}
}
