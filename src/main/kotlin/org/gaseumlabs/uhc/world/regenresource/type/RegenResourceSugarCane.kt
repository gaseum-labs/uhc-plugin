package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.regenresource.*
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.superSurfaceSpreader

class RegenResourceSugarCane(
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
		return player.location.y >= 58
	}
	override fun onUpdate(vein: VeinBlock) {}

	override fun generate(genBounds: RegenUtil.GenBounds, fullVein: Boolean): GenResult? {
		val surface = superSurfaceSpreader(genBounds) { block ->
			Util.binarySearch(block.type, growable) && (
				waterSource(block.getRelative(1, 0, 0)) ||
				waterSource(block.getRelative(0, 0, 1)) ||
				waterSource(block.getRelative(-1, 0, 0)) ||
				waterSource(block.getRelative(0, 0, -1))
			)
		}.randomOrNull() ?: return null

		return if (fullVein) GenResult(
			listOf(
				surface,
				surface.getRelative(0, 1, 0),
				surface.getRelative(0, 2, 0),
				surface.getRelative(0, 3, 0),
			), 3
		) else GenResult(
			listOf(
				surface,
				surface.getRelative(0, 1, 0),
			), 1
		)
	}

	override fun initializeBlock(blocks: List<Block>, fullVein: Boolean) {
		blocks.slice(1 until blocks.size).forEach { it.setType(SUGAR_CANE, false) }
	}

	override fun isModifiedBlock(blocks: List<Block>) = !Util.binarySearch(
		blocks[0].type, growable
	) || blocks.slice(1 until blocks.size).any { it.type !== SUGAR_CANE }

	/* placement */

	companion object {
		private fun waterSource(block: Block) = block.type === ICE || SpawnUtil.isWater(block)

		private val growable = Util.sortedArrayOf(
			GRASS_BLOCK,
			DIRT,
			SAND,
			RED_SAND,
			COARSE_DIRT,
			PODZOL,
		)
	}
}