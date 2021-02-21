package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.ChatColor
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

class OrePlacer(size: Int, uniqueSeed: Int, private val low: Int, private val high: Int, private val amount: Int, val type: Material) : DelayedChunkPlacer(size, uniqueSeed) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (x in -1..1) for (z in -1..1)
			if (!world.isChunkGenerated(chunkX + x, chunkZ + z)) return false

		return true
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, low, high) { block, _, _, _ ->
			if (block.type == Material.STONE) {
				if (
					isOpen(block.getRelative(BlockFace. DOWN).type) ||
					isOpen(block.getRelative(BlockFace.   UP).type) ||
					isOpen(block.getRelative(BlockFace. EAST).type) ||
					isOpen(block.getRelative(BlockFace. WEST).type) ||
					isOpen(block.getRelative(BlockFace.NORTH).type) ||
					isOpen(block.getRelative(BlockFace.SOUTH).type)
				) {
					val veinBlocks = Array(amount) { block }

					/* place the first ore in the vein */
					block.setType(type, false)

					/* place the rest in a contiguous cluster */
					for (j in 1 until amount) {
						/* get an existing position */
						var positionIndex = Util.randRange(0, j - 1)
						val startIndex = positionIndex

						var nextBlock: Block? = null

						while (nextBlock == null) {
							val thisBlock = veinBlocks[positionIndex]
							nextBlock = placeRelativeBlock(thisBlock)

							if (nextBlock == null) {
								positionIndex = (positionIndex + 1) % amount
								/* no more ores can be placed contiguously */
								if (positionIndex == startIndex) return@randomPosition true
							}
						}

						veinBlocks[j] = nextBlock
					}

					true
				} else {
					false
				}
			} else {
				false
			}
		}
	}

	/**
	 * replaces the a block adjacent to this x y z location if possible
	 * returns the block
	 *
	 * @return null the block couldn't be placed
	 */
	private fun placeRelativeBlock(block: Block): Block? {
		var faceIndex = Util.randRange(0, 5)
		val startIndex = faceIndex

		var relativeBlock = block.getRelative(BlockFace.values()[faceIndex])

		while (relativeBlock.type != Material.STONE) {
			faceIndex = (faceIndex + 1) % 6
			/* every single face is covered */
			if (faceIndex == startIndex) return null

			relativeBlock = block.getRelative(BlockFace.values()[faceIndex])
		}

		relativeBlock.setType(type, false)

		return relativeBlock
	}

	private fun isOpen(type: Material): Boolean {
		return when (type) {
			Material.AIR -> true
			Material.CAVE_AIR -> true
			Material.WATER -> true
			else -> false
		}
	}
}
