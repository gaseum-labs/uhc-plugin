package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.*

class MelonRegen(game: Game) : Regen(game, 3, 2400) {
	/* at least half of the chunk must be jungle */
	fun hasJungle(chunk: Chunk): Boolean {
		val THRESHOLD = 8 * 8 / 2
		var count = 0

		for (x in 0..7) for (z in 0..7) {
			val biome = chunk.getBlock(x * 2, 63, z * 2).biome

			if (
				biome === Biome.JUNGLE ||
				biome === Biome.JUNGLE_HILLS ||
				biome === Biome.MODIFIED_JUNGLE ||
				biome === Biome.BAMBOO_JUNGLE ||
				biome === Biome.BAMBOO_JUNGLE_HILLS ||
				biome === Biome.JUNGLE_EDGE ||
				biome === Biome.MODIFIED_JUNGLE_EDGE
			)
				++count

			if (count >= THRESHOLD) return true
		}

		return false
	}

	override fun place(chunk: Chunk): Boolean {
		if (!hasJungle(chunk)) return false

		val block = AbstractChunkPlacer.randomPosition(chunk, 63, 80) { block, x, y, z ->
			block.isPassable && block.getRelative(BlockFace.DOWN).type === Material.GRASS_BLOCK
		}

		/* update physics */
		block?.type = Material.MELON
		
		return block != null
	}
}
