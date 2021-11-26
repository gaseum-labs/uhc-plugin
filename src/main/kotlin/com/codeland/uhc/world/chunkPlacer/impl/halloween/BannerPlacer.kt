package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.*
import org.bukkit.block.*
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.block.data.Directional
import kotlin.random.Random

class BannerPlacer(size: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (i in -1..1) {
			for (j in -1..1) {
				if (!world.isChunkGenerated(chunkX + i, chunkZ + j)) return false
			}
		}

		return true
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, 20, 80) { block, x, y, z ->
			fun tryBanner(facing: BlockFace): Boolean {
				return if (Util.binarySearch(block.getRelative(facing).type, validBlocks)) {
					makeBanner(chunk.world, bannerList[Random.nextInt(bannerList.size)], facing.oppositeFace, block)
					true
				} else {
					false
				}
			}

			if (isAir(block) && isAir(block.getRelative(BlockFace.DOWN))) {
				when {
					tryBanner(BlockFace.NORTH) -> true
					tryBanner(BlockFace.EAST) -> true
					tryBanner(BlockFace.WEST) -> true
					tryBanner(BlockFace.SOUTH) -> true
					else -> false
				}
			} else {
				false
			}
		}
	}

	data class BannerData(val baseColor: Material, val patterns: Array<Pattern>)

	val bannerList = arrayOf(
		/* bat */
		BannerData(Material.BLACK_WALL_BANNER, arrayOf(
			Pattern(DyeColor.PURPLE, PatternType.CIRCLE_MIDDLE),
			Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER),
			Pattern(DyeColor.PURPLE, PatternType.TRIANGLE_TOP),
			Pattern(DyeColor.PURPLE, PatternType.TRIANGLES_BOTTOM)
		)),
		/* cat */
		BannerData(Material.BLACK_WALL_BANNER, arrayOf(
			Pattern(DyeColor.ORANGE, PatternType.CURLY_BORDER),
			Pattern(DyeColor.ORANGE, PatternType.SQUARE_TOP_LEFT),
			Pattern(DyeColor.ORANGE, PatternType.TRIANGLES_TOP),
			Pattern(DyeColor.LIME, PatternType.STRIPE_BOTTOM),
			Pattern(DyeColor.BLACK, PatternType.MOJANG)
		)),
		/* jack o lantern */
		BannerData(Material.YELLOW_WALL_BANNER, arrayOf(
			Pattern(DyeColor.ORANGE, PatternType.FLOWER),
			Pattern(DyeColor.YELLOW, PatternType.HALF_HORIZONTAL),
			Pattern(DyeColor.ORANGE, PatternType.TRIANGLE_TOP),
			Pattern(DyeColor.ORANGE, PatternType.STRIPE_MIDDLE),
			Pattern(DyeColor.ORANGE, PatternType.TRIANGLES_BOTTOM),
			Pattern(DyeColor.ORANGE, PatternType.CIRCLE_MIDDLE)
		)),
		/* bone */
		BannerData(Material.BLUE_WALL_BANNER, arrayOf(
			Pattern(DyeColor.WHITE, PatternType.STRIPE_CENTER),
			Pattern(DyeColor.WHITE, PatternType.TRIANGLE_TOP),
			Pattern(DyeColor.WHITE, PatternType.TRIANGLE_BOTTOM),
			Pattern(DyeColor.BLUE, PatternType.TRIANGLES_TOP),
			Pattern(DyeColor.BLUE, PatternType.TRIANGLES_BOTTOM),
			Pattern(DyeColor.BLUE, PatternType.CURLY_BORDER)
		)),
		/* goblin */
		BannerData(Material.BLACK_WALL_BANNER, arrayOf(
			Pattern(DyeColor.WHITE, PatternType.STRIPE_MIDDLE),
			Pattern(DyeColor.BLACK, PatternType.CIRCLE_MIDDLE),
			Pattern(DyeColor.GREEN, PatternType.CURLY_BORDER),
			Pattern(DyeColor.GREEN, PatternType.CREEPER),
			Pattern(DyeColor.GREEN, PatternType.STRIPE_CENTER),
			Pattern(DyeColor.GREEN, PatternType.STRIPE_TOP)
		)),
		/* bloody eyes */
		BannerData(Material.WHITE_WALL_BANNER, arrayOf(
			Pattern(DyeColor.RED, PatternType.STRIPE_SMALL),
			Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL),
			Pattern(DyeColor.WHITE, PatternType.GRADIENT_UP),
			Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE),
			Pattern(DyeColor.WHITE, PatternType.STRIPE_CENTER)
		))
	)

	fun makeBanner(world: World, bannerData: BannerData, facing: BlockFace, block: Block) {
		block.setType(bannerData.baseColor, false)
		val banner = block.getState(false) as Banner

		bannerData.patterns.forEach { pattern -> banner.addPattern(pattern) }
		banner.update(true, false)

		val data = block.blockData as Directional
		data.facing = facing
		block.blockData = data
	}

	fun isAir(block: Block): Boolean {
		return block.type == Material.AIR || block.type == Material.CAVE_AIR
	}

	val validBlocks = arrayOf(
		Material.STONE,
		Material.OAK_LOG,
		Material.BIRCH_LOG,
		Material.SPRUCE_LOG,
		Material.JUNGLE_LOG,
		Material.DARK_OAK_LOG,
		Material.ACACIA_LOG,
		Material.CRIMSON_STEM,
		Material.WARPED_STEM,
		Material.NETHERRACK
	)

	init {
		validBlocks.sort()
	}
}
