package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.util.IntVector
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.extensions.ArrayListExtensions.mapFirstNotNullPrefer
import org.gaseumlabs.uhc.util.extensions.BlockExtensions.samePlace
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionBlock
import kotlin.math.ceil
import kotlin.random.Random

class ResourceOre(
	val type: Material,
	val deepType: Material,
	val veinSize: Int,
	val yRange: IntRange,
	val yEligable: (y: Int) -> Boolean,
	val perfectGen: Boolean,

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
		return yEligable(player.location.y.toInt())
	}

	override fun generate(bounds: RegenUtil.GenBounds, fullVein: Boolean): List<Block>? {
		if (perfectGen) return generate2(bounds, fullVein)

		val potentialSpots = RegenUtil.volume(
			bounds,
			yRange,
			32
		) { block ->
			if (block.isPassable) block else null
		}

		val oreSource = potentialSpots.firstNotNullOfOrNull { startBlock ->
			RegenUtil.expandFrom(startBlock, 4) {
				when {
					isPass(it) -> false
					isReplaceable(it) -> true
					else -> null
				}
			}
		} ?: return null

		return createOreFrom(oreSource, if (fullVein) veinSize else 1)
	}

	fun isWall(block: Block) = block.type === Material.LAVA || block.isCollidable

	fun indexToXYZ(i: Int, wide: Int, tall: Int, deep: Int) = IntVector(
		i / (tall * deep),
		(i / deep) % tall,
		i % deep
	)

	fun xyzToIndex(x: Int, y: Int, z: Int, wide: Int, tall: Int, deep: Int) =
		x * tall * deep + y * deep + z

	fun generate2(bounds: RegenUtil.GenBounds, fullVein: Boolean): List<Block>? {
		val GAP = 4

		val wide = ceil(bounds.width / GAP.toFloat()).toInt()
		val tall = ceil((yRange.last - yRange.first + 1) / GAP.toFloat()).toInt()
		val deep = ceil(bounds.depth / GAP.toFloat()).toInt()

		val outerWide = wide + 2
		val outerTall = tall + 2
		val outerDeep = deep + 2

		val offX = Random.nextInt(GAP)
		val offY = Random.nextInt(GAP)
		val offZ = Random.nextInt(GAP)

		val grid = Array(outerWide * outerTall * outerDeep) { i ->
			val (x, y, z) = indexToXYZ(i, outerWide, outerTall, outerDeep)

			val block = bounds.world.getBlockAt(
				bounds.x     + offX + (x - 1) * GAP,
				yRange.first + offY + (y - 1) * GAP,
				bounds.z     + offZ + (z - 1) * GAP
			)

			if (isWall(block)) 1 else if (isPass(block)) 0 else 2
		}

		val visitOrder = Array(wide * tall * deep) { it }
		visitOrder.shuffle()

		for (i in visitOrder.indices) {
			val (x, y, z) = indexToXYZ(visitOrder[i], wide, tall, deep).add(1, 1, 1)

			if (grid[xyzToIndex(x, y, z, outerWide, outerTall, outerDeep)] == 0) {
				val expandFaces = aroundFaces.filter { face ->
					grid[xyzToIndex(x + face.modX, y + face.modY, z + face.modZ, outerWide, outerTall, outerDeep)] == 1
				}
				if (expandFaces.isEmpty()) continue

				val startBlock = bounds.world.getBlockAt(
					bounds.x     + offX + (x - 1) * GAP,
					yRange.first + offY + (y - 1) * GAP,
					bounds.z     + offZ + (z - 1) * GAP
				)

				val oreSource = RegenUtil.newExpandFrom(expandFaces, startBlock, 4) {
					when {
						isPass(it) -> false
						isReplaceable(it) -> true
						else -> null
					}
				} ?: continue

				return createOreFrom(oreSource, if (fullVein) veinSize else 1)
			}
		}

		return null
	}

	override fun setBlock(block: Block, index: Int, fullVein: Boolean) {
		block.setType(if (block.type === Material.DEEPSLATE || block.type === Material.TUFF) deepType else type, false)
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === type || block.type === deepType
	}

	/* placement */

	fun createOreFrom(origin: Block, numBlocks: Int): List<Block> {
		/* keep track of all the ores that will be placed */
		/* to decide the next location to spread to */
		val veinBlocks = ArrayList<Block>(numBlocks)
		veinBlocks.add(origin)

		/* place the rest in a contiguous cluster */
		for (j in 1 until numBlocks) {
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

			if (isReplaceable(relative)) {
				return face to null

			} else {
				nonOptimal = face
			}
		}

		return null to nonOptimal
	}

	companion object {
		val wallMaterials = Util.sortedArrayOf(
			Material.DRIPSTONE_BLOCK,
			Material.STONE,
			Material.ANDESITE,
			Material.DIORITE,
			Material.GRANITE,
			Material.TUFF,
			Material.DEEPSLATE,
			Material.COPPER_ORE,
			Material.DEEPSLATE_COPPER_ORE,
			Material.IRON_ORE,
			Material.DEEPSLATE_IRON_ORE,
			Material.COAL_ORE,
			Material.DEEPSLATE_COAL_ORE,
			Material.GOLD_ORE,
			Material.DEEPSLATE_GOLD_ORE,
			Material.REDSTONE_ORE,
			Material.DEEPSLATE_REDSTONE_ORE,
			Material.DIAMOND_ORE,
			Material.DEEPSLATE_DIAMOND_ORE,
			Material.LAPIS_ORE,
			Material.DEEPSLATE_LAPIS_ORE,
			Material.EMERALD_ORE,
			Material.DEEPSLATE_EMERALD_ORE,
			Material.CLAY,
			Material.MAGMA_BLOCK,
		)

		val replaceMaterials = Util.sortedArrayOf(
			Material.DRIPSTONE_BLOCK,
			Material.STONE,
			Material.ANDESITE,
			Material.DIORITE,
			Material.GRANITE,
			Material.TUFF,
			Material.DEEPSLATE,
			Material.COPPER_ORE,
			Material.DEEPSLATE_COPPER_ORE,
			Material.CLAY,

			Material.BLACKSTONE,
			Material.BASALT,
			Material.NETHERRACK,
			Material.NETHER_GOLD_ORE,
			Material.NETHER_QUARTZ_ORE,
			Material.SOUL_SAND,
			Material.SOUL_SOIL,
			Material.CRIMSON_NYLIUM,
			Material.WARPED_NYLIUM,
		)

		val passMaterials = Util.sortedArrayOf(
			Material.POINTED_DRIPSTONE,
			Material.BIG_DRIPLEAF,
			Material.AZALEA,
			Material.FLOWERING_AZALEA,
			Material.MOSS_CARPET,
			Material.SMALL_AMETHYST_BUD,
			Material.MEDIUM_AMETHYST_BUD,
			Material.LARGE_AMETHYST_BUD,
			Material.AMETHYST_CLUSTER,
		)

		fun isWall(block: Block) = Util.binarySearch(block.type, wallMaterials)

		fun isPass(block: Block) = (block.type !== Material.LAVA && block.isPassable) || Util.binarySearch(block.type, passMaterials)

		fun isReplaceable(block: Block) = Util.binarySearch(block.type, replaceMaterials)
	}
}
