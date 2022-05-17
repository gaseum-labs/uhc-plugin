package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.util.extensions.ArrayListExtensions.mapFirstNotNullPrefer
import org.gaseumlabs.uhc.util.extensions.BlockExtensions.samePlace
import org.gaseumlabs.uhc.util.extensions.IntRangeExtensions.rangeIntersection
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class ResourceOre(
	val type: Material,
	val deepType: Material,
	val veinSize: Int,
	val genRange: IntRange,

	initialReleased: Int,
	maxReleased: Int,
	maxCurrent: Int,
	interval: Int,
) : ResourceDescriptionBlock(
	initialReleased,
	maxReleased,
	maxCurrent,
	interval,
	type.name
) {
	companion object {
		const val Y_RANGE = 20
		const val MIN_Y = -54
		const val MAX_Y = 100
	}

	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		if (world !== WorldManager.gameWorld) return null

		val playerYRange = ((centerY - Y_RANGE).coerceIn(MIN_Y..MAX_Y)..(centerY + Y_RANGE).coerceIn(MIN_Y..MAX_Y))
			.rangeIntersection(genRange)

		if (playerYRange.isEmpty()) return null

		/* sampling approximately every 8 blocks in each column */
		val tries = ceil((playerYRange.last - playerYRange.first + 1) / 8.0f).toInt()

		/* spots in caves around you, near your y level and in the gen y level */
		val around = RegenUtil.locateAround(world, centerX, centerZ, 9, 24.0, 72.0, 6) { x, z ->
			for (i in 0 until tries) {
				val block = world.getBlockAt(x, playerYRange.random(), z)
				if (block.isPassable) return@locateAround block
			}

			return@locateAround null
		}

		val oreSource = around.firstNotNullOfOrNull { startBlock ->
			RegenUtil.expandFrom(startBlock, 4) { block ->
				if (block.isPassable) {
					false
				} else {
					if (isStone(block)) true else null
				}
			}
		} ?: return null

		return createOreFrom(oreSource)
	}

	override fun setBlock(block: Block) {
		block.setType(if (block.type === Material.DEEPSLATE) deepType else type, false)
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === type || block.type === deepType
	}

	/* placement */

	fun createOreFrom(origin: Block): List<Block> {
		/* keep track of all the ores that will be placed */
		/* to decide the next location to spread to */
		val veinBlocks = ArrayList<Block>(veinSize)
		veinBlocks.add(origin)

		/* place the rest in a contiguous cluster */
		for (j in 1 until veinSize) {
			veinBlocks.shuffle()
			veinBlocks.add(
				veinBlocks.mapFirstNotNullPrefer { oreBlock ->
					val (optimal, nonOptimal) = openFace(oreBlock, veinBlocks)

					Pair(
						if (optimal != null) oreBlock.getRelative(optimal) else null,
						if (nonOptimal != null) oreBlock.getRelative(nonOptimal) else null
					)
				} ?: return veinBlocks
			)
		}

		return veinBlocks
	}

	private val aroundFaces = arrayOf(
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
	private fun openFace(oreBlock: Block, currentVein: List<Block>): Pair<BlockFace?, BlockFace?> {
		var nonOptimal: BlockFace? = null

		aroundFaces.shuffle()
		for (face in aroundFaces) {
			val relative = oreBlock.getRelative(face)
			/* do NOT tread back into an already placed ore */
			if (currentVein.any { it.samePlace(relative) }) continue

			if (isStone(relative)) {
				return face to null

			} else {
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
}