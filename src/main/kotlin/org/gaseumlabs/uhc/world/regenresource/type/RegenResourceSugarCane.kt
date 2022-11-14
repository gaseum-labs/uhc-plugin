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
	released: HashMap<PhaseType, Release>,
	worldName: String,
	prettyName: String,
) : RegenResourceBlock(
	released,
	worldName,
	prettyName,
) {
	override fun eligible(player: Player): Boolean {
		return player.location.y >= 58
	}
	override fun onUpdate(vein: VeinBlock) {}

	override fun generate(genBounds: RegenUtil.GenBounds, tier: Int): GenResult? {
		val surface = superSurfaceSpreader(genBounds) { block ->
			Util.binarySearch(block.type, growable) && (
				waterSource(block.getRelative(1, 0, 0)) ||
				waterSource(block.getRelative(0, 0, 1)) ||
				waterSource(block.getRelative(-1, 0, 0)) ||
				waterSource(block.getRelative(0, 0, -1))
			)
		}.randomOrNull() ?: return null

		val height = (3 - tier).coerceAtLeast(1)

		val list = arrayListOf(surface)
		for (i in 0 until height) list.add(surface.getRelative(0, i + 1, 0))
		return GenResult(list, height)
	}

	override fun initializeBlock(blocks: List<Block>, tier: Int) {
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