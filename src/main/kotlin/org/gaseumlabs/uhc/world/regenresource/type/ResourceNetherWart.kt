package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderNether
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionBlock

class ResourceNetherWart(
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

	override fun generate(bounds: RegenUtil.GenBounds, fullVein: Boolean): List<Block>? {
		val potentialSpots = RegenUtil.volume(
			bounds,
			32..110,
			32
		) { block ->
			if (block.isPassable) block else null
		}

		for (block in potentialSpots) {
			val surface = surfaceSpreaderNether(bounds.world, block.x, block.y, block.z, 5, ::wartGood)
			if (surface != null) {
				return listOf(
					surface,
					surface.getRelative(UP)
				)
			}
		}

		return null
	}

	override fun setBlock(block: Block, index: Int, fullVein: Boolean) {
		if (index == 0) {
			block.setType(SOUL_SAND, false)
		} else {
			block.setType(NETHER_WART, false)
			val data = block.blockData as Ageable
			data.age = if (fullVein) 3 else 0
			block.setBlockData(data, false)
		}
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === NETHER_WART || block.type === SOUL_SAND
	}

	/* placement */

	private fun wartGood(surfaceBlock: Block): Boolean {
		if (!(surfaceBlock.type === BASALT ||
			surfaceBlock.type === MAGMA_BLOCK ||
			surfaceBlock.type === SOUL_SAND ||
			surfaceBlock.type === SOUL_SOIL ||
			surfaceBlock.type === GRAVEL ||
			surfaceBlock.type === WARPED_NYLIUM ||
			surfaceBlock.type === CRIMSON_NYLIUM)
		) return false

		val up = surfaceBlock.getRelative(BlockFace.UP)

		return up.isPassable && !up.isLiquid
	}
}
