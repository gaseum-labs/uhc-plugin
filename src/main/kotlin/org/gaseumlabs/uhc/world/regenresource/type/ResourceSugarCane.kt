package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.superSurfaceSpreader
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderOverworld
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionBlock

class ResourceSugarCane(
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
		return player.location.y >= 58
	}

	override fun generate(bounds: RegenUtil.GenBounds, fullVein: Boolean): List<Block>? {
		//val potentialBlocks = superSurfaceSpreader(bounds) { block ->
		//	block
		//}
		val surface = surfaceSpreaderOverworld(
			bounds.world,
			bounds.centerX(),
			bounds.centerZ(),
			9,
			::sugarCaneGood
		)
		if (surface != null) {
			return if (fullVein) listOf(
				surface.getRelative(0, 1, 0),
				surface.getRelative(0, 2, 0),
				surface.getRelative(0, 3, 0),
			) else listOf(
				surface.getRelative(0, 1, 0)
			)
		}

		return null
	}

	override fun setBlock(block: Block, index: Int, fullVein: Boolean) {
		block.setType(SUGAR_CANE, false)
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === SUGAR_CANE
	}

	/* placement */

	companion object {
		val growable = Util.sortedArrayOf(
			GRASS_BLOCK,
			SAND,
			RED_SAND,
			COARSE_DIRT,
			PODZOL,
		)
	}

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