package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.world.regenresource.*
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderNether

class RegenResourceNetherWart(
	released: HashMap<PhaseType, Release>,
	worldName: String,
	prettyName: String,
) : RegenResourceBlock(
	released,
	worldName,
	prettyName,
) {
	override fun eligible(player: Player): Boolean {
		return true
	}
	override fun onUpdate(vein: VeinBlock) {}

	override fun generate(genBounds: RegenUtil.GenBounds, tier: Int): GenResult? {
		val potentialSpots = RegenUtil.volume(
			genBounds,
			32..110,
			32
		) { block ->
			if (block.isPassable) block else null
		}

		for (block in potentialSpots) {
			val surface = surfaceSpreaderNether(genBounds.world, block.x, block.y, block.z, 5, ::wartGood)
			if (surface != null) {
				return GenResult(listOf(
					surface,
					surface.getRelative(UP)
				), 1)
			}
		}

		return null
	}

	override fun initializeBlock(blocks: List<Block>, tier: Int) {
		blocks[0].setType(SOUL_SAND, false)

		blocks[1].setType(NETHER_WART, false)
		val data = blocks[1].blockData as Ageable
		data.age = if (tier == 0) 3 else 0
		blocks[1].setBlockData(data, false)
	}

	override fun isModifiedBlock(blocks: List<Block>) =
		blocks[0].type !== SOUL_SAND || blocks[1].type !== NETHER_WART

	private fun wartGood(surfaceBlock: Block): Boolean {
		if (!(surfaceBlock.type === BASALT ||
			surfaceBlock.type === MAGMA_BLOCK ||
			surfaceBlock.type === SOUL_SAND ||
			surfaceBlock.type === SOUL_SOIL ||
			surfaceBlock.type === GRAVEL ||
			surfaceBlock.type === WARPED_NYLIUM ||
			surfaceBlock.type === CRIMSON_NYLIUM)
		) return false

		val up = surfaceBlock.getRelative(UP)

		return up.isPassable && !up.isLiquid
	}
}
