package org.gaseumlabs.uhc.world.regenresource.type

import net.minecraft.world.level.biome.Biomes
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.extensions.ArrayExtensions.shuffled
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.ResourceDescription
import kotlin.math.*
import kotlin.random.Random

class ResourceMelon : ResourceDescription() {
	override fun nextInterval(collected: Int): Int {
		return 20 * (/* 120 */30 + 30 * collected)
	}

	override fun maxCurrent(collected: Int): Int {
		return 4
	}

	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		if (world !== WorldManager.gameWorld) return null

		val worldHandle = (world as CraftWorld).handle

		/* a place on grass without any cover */
		var fallbackBlock: Block? = null

		val potentialBlocks = locateJunglesAround(world, centerX, centerZ, centerY, 11, 32.0, 80.0, 8)

		potentialBlocks.forEach { (x, z)  ->
			val fallingBlock = world.spawnFallingBlock(Location(world, x + 0.5, centerY.toDouble(), z + 0.5), Material.LAPIS_BLOCK.createBlockData())
			fallingBlock.setGravity(false)
			fallingBlock.isGlowing = true
			fallingBlock.dropItem = false
		}

		for ((x, z) in potentialBlocks) {
			val initialBlock = world.getBlockAt(x, centerY, z)

			val melonSurface = surfaceSpreader(initialBlock, 20, 5, 5, ::jungleSurface, ::melonGood)
			if (melonSurface != null) {
				return listOf(melonSurface.getRelative(BlockFace.UP))
			}
		}

		return null
	}

	/**
	 * gives block positions at scanY
	 */
	fun locateJunglesAround(
		world: World,
		centerX: Int,
		centerZ: Int,
		scanY: Int,
		scanSize: Int,
		minRadius: Double,
		maxRadius: Double,
		maxTries: Int,
	): ArrayList<Pair<Int, Int>> {
		val worldHandle = (world as CraftWorld).handle

		/*
		 * find biomes at the player's y level
		 * in concentric circles which are each about scanSize larger than the next
		 * starting with radius minRadius, up to radius maxRadius
		 */
		val numCircles = ceil((maxRadius - minRadius) / scanSize).toInt()

		/*
		 * all the points we check in concentric circles
		 * the order of the circles, and the points are randomized
		 */
		val points = (0 until numCircles).flatMap { i ->
			val radius = Util.interp(
				minRadius,
				maxRadius,
				Util.invInterp(0.0, numCircles - 1.0, i.toDouble())
			)

			val circumferenceLength = 2.0 * PI * radius
			val numPoints = (circumferenceLength / scanSize).roundToInt()

			val startAngle = Random.nextDouble(0.0, 2 * PI).toFloat()

			Array(numPoints) { j ->
				val alongCircumference = Util.invInterp(0.0, numPoints.toDouble(), j.toDouble())

				val x = floor(centerX + 0.5 + radius * cos(startAngle + alongCircumference * 2.0 * PI)).toInt()
				val z = floor(centerZ + 0.5 + radius * sin(startAngle + alongCircumference * 2.0 * PI)).toInt()

				x to z
			}.asIterable()
		} as ArrayList<Pair<Int, Int>>
		points.shuffle()

		val ret = ArrayList<Pair<Int, Int>>(maxTries)

		for ((x, z) in points) {
			if (!insideWorldBorder(world, x, z)) continue

			val biome = worldHandle.getNoiseBiome(x / 4, scanY / 4, z / 4).unwrapKey().get()
			if (
				biome === Biomes.JUNGLE_UHC ||
				biome === Biomes.SPARSE_JUNGLE_UHC ||
				biome === Biomes.BAMBOO_JUNGLE_UHC
			) ret.add(x to z)

			if (ret.size >= maxTries) return ret
		}

		return ret
	}

	fun insideWorldBorder(world: World, x: Int, z: Int): Boolean {
		val radius = (world.worldBorder.size / 2.0).toInt()
		return x in -radius..radius && z in -radius..radius
	}

	fun jungleSurface(block: Block): Boolean {
		if (block.type === Material.GRASS_BLOCK) return true
		if (block.type !== Material.DIRT) return false

		return when (block.getRelative(BlockFace.UP).type) {
			Material.OAK_LOG,
			Material.JUNGLE_LOG -> true
			else -> false
		}
	}

	fun melonGood(surfaceBlock: Block): Boolean {
		val above = surfaceBlock.getRelative(BlockFace.UP)
		val ceiling = above.getRelative(BlockFace.UP)

		return when (surfaceBlock.getRelative(BlockFace.UP).type) {
			Material.AIR,
			Material.FERN,
			Material.VINE,
			Material.COCOA,
			Material.OAK_LEAVES,
			Material.JUNGLE_LEAVES,
			Material.GRASS -> true
			else -> false
		} && !ceiling.isPassable && ceiling.type !== Material.COCOA
	}

	fun findSurfaceFrom(
		block: Block,
		yRange: Int,
		isSurface: (block: Block) -> Boolean,
	): Block? {
		if (isSurface(block)) return block

		for (i in 1 ..yRange) {
			val above = block.getRelative(0, i, 0)
			if (isSurface(above)) return above

			val below = block.getRelative(0, -i, 0)
			if (isSurface(below)) return below
		}

		return null
	}

	fun surfaceSpreader(
		startBlock: Block,
		initialYRange: Int,
		yRange: Int,
		spread: Int,
		isSurface: (block: Block) -> Boolean,
		isGood: (block: Block) -> Boolean,
	): Block? {
		val initialBlock = findSurfaceFrom(startBlock, initialYRange, isSurface)
			?: return null

		if (isGood(initialBlock)) return initialBlock

		val previousBlocks = Array(8) { initialBlock }

		for (i in 1 ..spread) {
			for (j in 0 until 8) {
				val (ox, oz) = when (j) {
					0 -> 1 to 0
					1 -> 0 to 1
					2 -> -1 to 0
					3 -> 0 to -1
					4 -> 1 to 1
					5 -> -1 to -1
					6 -> 1 to -1
					else -> -1 to 1
				}
				val previousBlock = previousBlocks[j] ?: continue
				val nextBlock = findSurfaceFrom(previousBlock.getRelative(ox, 0, oz), yRange, isSurface) ?: continue

				if (isGood(nextBlock)) {
					return nextBlock
				} else {
					previousBlocks[j] = nextBlock
				}
			}
		}

		return null
	}

	override fun setBlock(block: Block) {
		block.setType(Material.MELON, false)
	}
}
