package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import kotlin.random.Random

class OrePlacer(
	size: Int,
	private val low: Int,
	private val high: Int,
	private val amount: Int,
	val type: Material,
	val deepType: Material,
) : DelayedChunkPlacer(size) {
	val random = Random(size + low + high + amount + type.ordinal)

	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return chunkReadyAround(world, chunkX, chunkZ)
	}

	override fun place(chunk: Chunk) {
		randomPositionBool(chunk, low, high) { block ->
			if (
				isStone(block) && (
				isOpen(block.getRelative(BlockFace.DOWN).type) ||
				isOpen(block.getRelative(BlockFace.UP).type) ||
				isOpen(block.getRelative(BlockFace.EAST).type) ||
				isOpen(block.getRelative(BlockFace.WEST).type) ||
				isOpen(block.getRelative(BlockFace.NORTH).type) ||
				isOpen(block.getRelative(BlockFace.SOUTH).type)
				)
			) {
				/* place the first ore in the vein */
				placeOre(block)
				val veinBlocks = Array(amount) { block }

				/* place the rest in a contiguous cluster */
				for (j in 1 until amount) {
					/* get an existing block in the vein */
					var index = random.nextInt(0, j)
					val startIndex = index

					var currentBlock: Block? = placeRelativeBlock(veinBlocks[index])

					while (currentBlock == null) {
						index = (index + 1) % amount
						if (index == startIndex) return@randomPositionBool true

						currentBlock = placeRelativeBlock(veinBlocks[index])
					}

					veinBlocks[j] = currentBlock
				}

				true

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
		var faceIndex = random.nextInt(0, 6)
		val startIndex = faceIndex

		/* try to place a block on the first available face */
		/* if cannot find an available face return null */
		while (!isStone(block.getRelative(BlockFace.values()[faceIndex]))) {
			faceIndex = (faceIndex + 1) % 6
			if (faceIndex == startIndex) return null
		}

		return placeOre(block.getRelative(BlockFace.values()[faceIndex]))
	}

	private fun isStone(block: Block): Boolean {
		return block.type === Material.STONE ||
		block.type === Material.ANDESITE ||
		block.type === Material.GRANITE ||
		block.type === Material.DIORITE ||
		block.type === Material.TUFF ||
		block.type === Material.DEEPSLATE ||
		block.type === Material.BLACKSTONE ||
		block.type === Material.BASALT ||
		block.type === Material.MAGMA_BLOCK
	}

	private fun placeOre(block: Block): Block {
		block.setType(if (block.type === Material.DEEPSLATE) deepType else type, false)
		return block
	}

	private fun isOpen(type: Material): Boolean {
		return when (type) {
			Material.AIR -> true
			Material.CAVE_AIR -> true
			Material.WATER -> true
			else -> false
		}
	}

	companion object {
		fun removeOres(chunk: Chunk) {
			for (x in 0..15) for (z in 0..15) for (y in 1..127) {
				val block = chunk.getBlock(x, y, z)
				when (block.type) {
					Material.GOLD_ORE,
					Material.LAPIS_ORE,
					Material.DIAMOND_ORE,
					Material.DEEPSLATE_GOLD_ORE,
					Material.DEEPSLATE_LAPIS_ORE,
					Material.DEEPSLATE_DIAMOND_ORE,
					-> block.setType(Material.STONE, false)
					else -> {
					}
				}
			}
		}
	}
}
