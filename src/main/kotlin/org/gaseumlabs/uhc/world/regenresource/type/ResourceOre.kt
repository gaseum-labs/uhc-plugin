package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.*
import org.bukkit.Material.GOLD_ORE
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
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
	val yDistribution: (y: Float) -> Int,

	released: HashMap<PhaseType, Int>,
	chunkRadius: Int,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : ResourceDescriptionBlock(
	released,
	chunkRadius,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	override fun eligable(player: Player): Boolean {
		return true
	}

	override fun generateInChunk(chunk: Chunk): List<Block>? {
		val potentialSpots = RegenUtil.aroundInChunk(
			chunk,
			yDistribution,
			32
		) { block ->
			if (block.isPassable) block else null
		}

		val oreSource = potentialSpots.firstNotNullOfOrNull { startBlock ->
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

	override fun setBlock(block: Block, index: Int) {
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
		block.type === Material.SOUL_SAND ||
		block.type === Material.SOUL_SOIL ||
		block.type === Material.NETHERRACK ||
		block.type === Material.BLACKSTONE ||
		block.type === Material.BASALT ||
		block.type === Material.MAGMA_BLOCK
	}
}