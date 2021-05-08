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
		return world.isChunkGenerated(chunkX + 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX + 1, chunkZ - 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ - 1)
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		fun hasJungle(): Boolean {
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

		fun validSide(block: Block, blockFace: BlockFace): Boolean {
			var checkBlock = block

			for (i in 0..3) {
				checkBlock = checkBlock.getRelative(blockFace)

				if (checkBlock.isPassable) {
					val up = checkBlock.getRelative(BlockFace.UP)

					/* if there's an air gap, the wall block must come immediately next */
					if (up.isPassable) return (!checkBlock.getRelative(blockFace).isPassable)
					/* if there's a roof, continue */
				} else {
					/* if there's a wall, good */
					return true
				}
			}

			/* 4 continuous roof blocks is good */
			return true
		}

		fun placeVine(block: Block, blockFace: BlockFace) {
			val vineBlock = block.getRelative(blockFace)

			if (vineBlock.isPassable) {
				if (vineBlock.type !== Material.VINE) vineBlock.setType(Material.VINE, false)

				val blockData = vineBlock.blockData as MultipleFacing
				blockData.setFace(blockFace.oppositeFace, true)
				vineBlock.blockData = blockData
			}
		}

		if (!hasJungle()) return

		randomPosition(chunk, 63, 80) { block, x, y, z ->
			if (
				block.isPassable &&
				block.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK &&
				!block.getRelative(BlockFace.UP).isPassable
			) {
				if (
					validSide(block, BlockFace.EAST) &&
					validSide(block, BlockFace.WEST) &&
					validSide(block, BlockFace.SOUTH) &&
					validSide(block, BlockFace.NORTH)
				) {
					block.setType(Material.MELON, false)
					block.getRelative(BlockFace.DOWN).setType(Material.DIRT, false)

					placeVine(block, BlockFace.EAST)
					placeVine(block, BlockFace.WEST)
					placeVine(block, BlockFace.SOUTH)
					placeVine(block, BlockFace.NORTH)

					true
				} else {
					false
				}
			} else {
				false
			}
		}
	}
}
