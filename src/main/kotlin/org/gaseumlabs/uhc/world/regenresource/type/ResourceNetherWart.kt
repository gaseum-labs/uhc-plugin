package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.*
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.data.Ageable
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.locateAround
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderNether
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionBlock

class ResourceNetherWart : ResourceDescriptionBlock(
	initialReleased = 1,
	maxReleased = 7,
	maxCurrent = 3,
	interval = 20 * 10,
	"Nether Wart"
) {
	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		if (world !== WorldManager.netherWorld) return null

		val potentialSpots = locateAround(world, centerX, centerZ, 11, 32.0, 80.0, 8) { x, z -> x to z }

		for ((x, z) in potentialSpots) {
			val surface = surfaceSpreaderNether(world, x, centerY, z, 5, ::wartGood)
			if (surface != null) {
				return listOf(
					surface,
					surface.getRelative(BlockFace.UP)
				)
			}
		}

		return null
	}

	override fun setBlock(block: Block, index: Int) {
		if (index == 0) {
			block.setType(SOUL_SAND, false)
		} else {
			block.setType(NETHER_WART, false)
			val data = block.blockData as Ageable
			data.age = 3
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
