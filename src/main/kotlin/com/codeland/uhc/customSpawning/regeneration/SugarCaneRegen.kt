package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.core.phase.phases.Shrink
import com.codeland.uhc.customSpawning.SpawnInfo.Companion.isWater
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import kotlin.math.ceil
import kotlin.random.Random

class SugarCaneRegen(game: Game): Regen(game, 170) {
	fun findCane(chunk: Chunk): Block? {
		val caneBlock = AbstractChunkPlacer.randomPosition(chunk, 58, 70) { block, _, _, _ ->
			block.type === Material.SUGAR_CANE
		}

		if (caneBlock != null) {
			var min: Block = caneBlock
			var max: Block = caneBlock

			while (min.getRelative(BlockFace.DOWN).type === Material.SUGAR_CANE) min = min.getRelative(BlockFace.DOWN)
			while (max.getRelative(BlockFace.UP).type === Material.SUGAR_CANE) max = max.getRelative(BlockFace.UP)

			/* the max sugar cane height is 4 */
			return if (max.y - min.y < 3) max.getRelative(BlockFace.UP) else null
		}

		return null
	}

	override fun place(chunk: Chunk): Boolean {
		val block = findCane(chunk) ?: AbstractChunkPlacer.randomPosition(chunk, 58, 70) { block, _, _, _ ->
			val down = block.getRelative(BlockFace.DOWN)

			(
				block.type.isAir || block.type == Material.GRASS
				) && (
				down.type === Material.GRASS_BLOCK ||
					down.type === Material.DIRT        ||
					down.type === Material.SAND        ||
					down.type === Material.PODZOL      ||
					down.type === Material.RED_SAND    ||
					down.type === Material.COARSE_DIRT
				) && (
				isWater(down.getRelative(BlockFace.WEST))  ||
					isWater(down.getRelative(BlockFace.EAST))  ||
					isWater(down.getRelative(BlockFace.NORTH)) ||
					isWater(down.getRelative(BlockFace.SOUTH))
				)
		}

		block?.setType(Material.SUGAR_CANE, false)

		return block != null
	}
}