package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

class MelonPlacer(size: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return world.isChunkGenerated(chunkX + 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX + 1, chunkZ - 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ - 1)
	}

	val TOP_SCORE = 100
	val OVERHANG_SCORE = 10
	val SURROUND_SCORE = 1

	val PERFECT_SCORE1 = TOP_SCORE + OVERHANG_SCORE * 4
	val PERFECT_SCORE2 = TOP_SCORE + SURROUND_SCORE * 4

	override fun place(chunk: Chunk, chunkIndex: Int) {
		var bestScore = 0
		var bestBlock: Block? = null

		fun sideScore(block: Block, blockFace: BlockFace): Int {
			val relative = block.getRelative(blockFace)

			return if (relative.isPassable && !relative.getRelative(BlockFace.UP).isPassable)
				OVERHANG_SCORE
			else if (!relative.isPassable)
				SURROUND_SCORE
			else
				0
		}

		fun hasJungle(): Boolean {
			for (x in 0..7) for (z in 0..7) {
				val biome = chunk.getBlock(x * 2, 63, z * 2).biome

				if (
					biome === Biome.JUNGLE ||
					biome === Biome.JUNGLE_HILLS ||
					biome === Biome.MODIFIED_JUNGLE ||
					biome === Biome.BAMBOO_JUNGLE ||
					biome === Biome.BAMBOO_JUNGLE_HILLS
				) return true
			}

			return false
		}

		if (!hasJungle()) return

		randomPosition(chunk, 63, 80) { block, x, y, z ->
			if (block.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK && block.isPassable) {
				val score = (if (!block.getRelative(BlockFace.UP).isPassable) TOP_SCORE else 0) +
					sideScore(block, BlockFace.EAST) +
					sideScore(block, BlockFace.WEST) +
					sideScore(block, BlockFace.SOUTH) +
					sideScore(block, BlockFace.NORTH)

				if (score > bestScore) {
					bestBlock = block
					bestScore = score

					if (score == PERFECT_SCORE1 || score == PERFECT_SCORE2) return@randomPosition true
				}
			}

			false
		}

		bestBlock?.setType(Material.MELON, false)
		bestBlock?.getRelative(BlockFace.DOWN)?.setType(Material.DIRT, false)
	}
}
