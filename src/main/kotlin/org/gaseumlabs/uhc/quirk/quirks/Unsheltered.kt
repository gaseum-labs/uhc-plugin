package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.metadata.FixedMetadataValue

class Unsheltered(type: QuirkType, game: Game) : Quirk(type, game) {
	override fun customDestroy() {}

	companion object {
		const val TAG_NAME = "unsh_b"

		val acceptedBlocks = arrayOf(
			Material.CRAFTING_TABLE,
			Material.FURNACE,
			Material.BREWING_STAND,
			Material.WHEAT_SEEDS,
			Material.BLAST_FURNACE,
			Material.SMOKER,
			Material.WATER,
			Material.LAVA,
			Material.LADDER,
			Material.ENCHANTING_TABLE,
			Material.BOOKSHELF,
			Material.SMITHING_TABLE,
			Material.LOOM,
			Material.ANVIL,
			Material.FLETCHING_TABLE,
			Material.COMPOSTER,
			Material.CHEST,
			Material.BARREL,
			Material.WET_SPONGE,
			Material.TNT
		)

		init {
			acceptedBlocks.sort()
		}

		fun isBroken(block: Block): Boolean {
			var broken = block.state.getMetadata(TAG_NAME)

			return broken.size != 0 && broken[0].asBoolean()
		}

		fun setBroken(block: Block, broken: Boolean) {
			block.state.setMetadata(TAG_NAME, FixedMetadataValue(org.gaseumlabs.uhc.UHCPlugin.plugin, broken))
		}
	}
}