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
import org.gaseumlabs.uhc.world.regenresource.*
import kotlin.math.ceil
import kotlin.random.Random

class RegenResourceOre(
	val type: Material,
	val deepType: Material,
	val veinSize: Int,
	val yRange: IntRange,
	val yEligable: (y: Int) -> Boolean,
	val perfectGen: Boolean,

	released: HashMap<PhaseType, Int>,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : RegenResourceBlock(
	released,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	override fun eligible(player: Player): Boolean {
		return yEligable(player.location.y.toInt())
	}
	override fun onUpdate(vein: VeinBlock) {}

	override fun generate(genBounds: RegenUtil.GenBounds, fullVein: Boolean): GenResult? {
		if (perfectGen) return generate2(genBounds, fullVein)

		val potentialSpots = RegenUtil.volume(
			genBounds,
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

		val size = if (fullVein) veinSize else 1
		return GenResult(createOreFrom(oreSource, size), size)
	}

	fun isWall(block: Block) = block.type === Material.LAVA || block.isCollidable


	fun generate2(bounds: RegenUtil.GenBounds, fullVein: Boolean): GenResult? {
		val source = RegenUtil.perfectGen(
			4,
			bounds,
			yRange,
			aroundFaces,
			{ if (isWall(it)) 1 else if (isPass(it)) 0 else 2 },
			{ when {
				isPass(it) -> false
				isReplaceable(it) -> true
				else -> null
			} }
		) ?: return null

		val size = if (fullVein) veinSize else 1
		return GenResult(createOreFrom(source, size), size)
	}

	override fun initializeBlock(blocks: List<Block>, fullVein: Boolean) {
		blocks.forEach {
			it.setType(if (it.type === Material.DEEPSLATE || it.type === Material.TUFF) deepType else type, false)
		}
	}

	override fun isModifiedBlock(blocks: List<Block>) = blocks.any {
		it.type !== type && it.type !== deepType
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
