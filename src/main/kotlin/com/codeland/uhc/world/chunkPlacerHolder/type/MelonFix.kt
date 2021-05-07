package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.MelonPlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder
import org.bukkit.Chunk
import org.bukkit.Material

class MelonFix : ChunkPlacerHolder() {
	companion object {
		fun removeMelons(chunk: Chunk) {
			for (x in 0..15) for (y in 63..80) for (z in 0..15) {
				val block = chunk.getBlock(x, y, z)
				if (block.type === Material.MELON) block.setType(Material.AIR, false)
			}
		}

		val melonPlacer = MelonPlacer(3)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(melonPlacer)
}
