package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material
import org.bukkit.Material.GOLD_ORE
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.FallingBlock
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
	val genRange: (y: Int) -> Boolean,
	val worldName: String,

	released: HashMap<PhaseType, Int>,
	current: Int,
	interval: Int,
	prettyName: String,
) : ResourceDescriptionBlock(
	released,
	current,
	interval,
	prettyName
) {
	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		if (world.name != worldName) return null

		val around = RegenUtil.sphereAround(world, centerX, centerY, centerZ, 48.0f, 64.0f, 20) { x, y, z ->
			if (!genRange(y)) return@sphereAround null
			val block = world.getBlockAt(x, y, z)
			if (block.isPassable) {
				block
			} else {
				null
			}
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