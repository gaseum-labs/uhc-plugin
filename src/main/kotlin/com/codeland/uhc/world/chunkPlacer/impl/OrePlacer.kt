package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.extensions.ArrayListExtensions.mapFirstNotNullPrefer
import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

class OrePlacer(
	perSquare: Int,
	squareSize: Int,
	private val low: Int,
	private val high: Int,
	private val amount: Int,
	val type: Material,
	val deepType: Material,
) : ChunkPlacer(perSquare, squareSize) {
	override fun place(chunk: Chunk) {
		val origin = randomPositionBool(chunk, low, high) { block ->
			isStone(block) && (
			isOpen(block.getRelative(BlockFace.DOWN).type) ||
			isOpen(block.getRelative(BlockFace.UP).type) ||
			isOpen(block.getRelative(BlockFace.EAST).type) ||
			isOpen(block.getRelative(BlockFace.WEST).type) ||
			isOpen(block.getRelative(BlockFace.NORTH).type) ||
			isOpen(block.getRelative(BlockFace.SOUTH).type)
			)
		} ?: randomSinglePosition(chunk, low, high)

		/* place the first ore in the vein */
		placeOre(origin)

		/* keep track of all the ores that will be placed */
		/* to decide the next location to spread to */
		val veinBlocks = ArrayList<Block>(amount)
		veinBlocks.add(origin)

		/* place the rest in a contiguous cluster */
		for (j in 1 until amount) {
			veinBlocks.shuffle()

			val (seedBlock, addFace) = veinBlocks.mapFirstNotNullPrefer { oreBlock ->
				val (optimal, nonOptimal) = openFace(oreBlock)
				optimal?.let { oreBlock to it } to nonOptimal?.let { oreBlock to it }
			} ?: return

			veinBlocks.add(placeOre(seedBlock.getRelative(addFace)))
		}
	}

	val aroundFaces = arrayOf(
		BlockFace.EAST,
		BlockFace.WEST,
		BlockFace.NORTH,
		BlockFace.SOUTH,
		BlockFace.DOWN,
		BlockFace.UP,
	)

	/**
	 * @return an optimal block face to place a new ore (is stone)
	 * and a non-optimal block face to place a new ore (any non-this ore)
	 */
	private fun openFace(oreBlock: Block): Pair<BlockFace?, BlockFace?> {
		var nonOptimal: BlockFace? = null

		aroundFaces.asIterable().shuffled().forEach { face ->
			val relative = oreBlock.getRelative(face)

			if (isStone(relative)) {
				return face to null

			} else if (relative.type !== type && relative.type !== deepType) {
				nonOptimal = face
			}
		}

		return null to nonOptimal
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
}
