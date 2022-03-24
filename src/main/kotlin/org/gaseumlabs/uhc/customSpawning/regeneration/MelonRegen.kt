package org.gaseumlabs.uhc.customSpawning.regeneration

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.*

class MelonRegen(game: Game) : Regen(game, 3, 2800) {
	/* at least half of the chunk must be jungle */
	private fun hasJungle(chunk: Chunk): Boolean {
		val THRESHOLD = 8 * 8 / 2
		var count = 0

		for (x in 0..7) for (z in 0..7) {
			val biome = chunk.getBlock(x * 2, 63, z * 2).biome

			if (SpawnUtil.jungle(biome)) ++count

			if (count >= THRESHOLD) return true
		}

		return false
	}

	override fun place(chunk: Chunk): Boolean {
		if (!hasJungle(chunk)) return false

		val block = ChunkPlacer.randomPositionBool(chunk, 63, 80) { block ->
			block.isPassable && block.getRelative(BlockFace.DOWN).type === Material.GRASS_BLOCK
		}

		/* update physics */
		block?.type = Material.MELON

		return block != null
	}
}
