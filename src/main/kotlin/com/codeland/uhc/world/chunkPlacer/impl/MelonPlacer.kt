package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.MultipleFacing

class MelonPlacer(size: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return chunkReadyAround(world, chunkX, chunkZ)
	}

	fun hasJungle(chunk: Chunk): Boolean {
		/* at least one fourth of the chunk must be jungle */
		val THRESHOLD = 8 * 8 / 4
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
			) ++count

			if (count >= THRESHOLD) return true
		}

		return false
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		if (!hasJungle(chunk)) return

		fun itb(boolean: Boolean) = if (boolean) 1 else 0

		var bestPosition = Pair(4, chunk.getBlock(0, 0, 0))

		randomPosition(chunk, 63, 80) { block, x, y, z ->
			if (
				block.isPassable &&
				block.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK &&
				!block.getRelative(BlockFace.UP).isPassable
			) {
				val numOpen = itb(block.getRelative(BlockFace.EAST).isPassable) +
					itb(block.getRelative(BlockFace.WEST).isPassable) +
					itb(block.getRelative(BlockFace.NORTH).isPassable) +
					itb(block.getRelative(BlockFace.SOUTH).isPassable)

				when {
					numOpen == 0 -> {
						bestPosition = Pair(0, block)
						true
					}
					numOpen < bestPosition.first -> {
						bestPosition = Pair(numOpen, block)
						false
					}
					else -> {
						false
					}
				}
			} else {
				false
			}
		}

		if (bestPosition.first <= 2) {
			for (x in -1..1) for (y in -1..1) for (z in -1..1) {
				val relative = bestPosition.second.getRelative(x, y, z)
				if (relative.isPassable) relative.setType(Material.JUNGLE_LEAVES, false)
			}
			bestPosition.second.setType(Material.MELON, false)
		}
	}
}
