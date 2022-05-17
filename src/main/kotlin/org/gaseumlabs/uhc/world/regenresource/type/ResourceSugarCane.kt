package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.*
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.UP
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.world.regenresource.*
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.locateAround
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreader

class ResourceSugarCane : ResourceDescriptionBlock(
	5,
	45,
	10,
	20 * 10,
) {
	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		val potentialBlocks = locateAround(world, centerX, centerZ, 11, 32.0, 80.0, 8) { x, z ->
			if (RegenUtil.insideWorldBorder(world, x, z)) x to z else null
		}

		for ((x, z) in potentialBlocks) {
			val surface = surfaceSpreader(world, x, z, 5, ::sugarCaneGood)
			if (surface != null) {
				return listOf(
					surface.getRelative(0, 1, 0),
					surface.getRelative(0, 2, 0),
					surface.getRelative(0, 3, 0),
				)
			}
		}

		return null
	}

	override fun setBlock(block: Block) {
		block.setType(SUGAR_CANE, false)
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === SUGAR_CANE
	}

	/* placement */

	private fun sugarCaneGood(surfaceBlock: Block): Boolean {
		if (!(surfaceBlock.type === GRASS_BLOCK ||
			surfaceBlock.type === SAND ||
			surfaceBlock.type === RED_SAND ||
			surfaceBlock.type === DIRT ||
			surfaceBlock.type === COARSE_DIRT ||
			surfaceBlock.type === PODZOL)
		) return false

		val up = surfaceBlock.getRelative(UP)
		if (up.isLiquid || !up.isPassable) return false

		return (
		SpawnUtil.isWater(surfaceBlock.getRelative(BlockFace.WEST)) ||
		SpawnUtil.isWater(surfaceBlock.getRelative(BlockFace.EAST)) ||
		SpawnUtil.isWater(surfaceBlock.getRelative(BlockFace.NORTH)) ||
		SpawnUtil.isWater(surfaceBlock.getRelative(BlockFace.SOUTH))
		)
	}
}