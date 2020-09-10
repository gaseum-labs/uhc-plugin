package com.codeland.uhc.core

import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Material

object OreFix {
	val ores = arrayOf(
		Material.COAL_ORE,
		Material.IRON_ORE,
		Material.GOLD_ORE,
		Material.DIAMOND_ORE,
		Material.LAPIS_ORE,
		Material.REDSTONE_ORE,
		Material.EMERALD_ORE
	)

	init {
		ores.sort()
	}

	fun removeOres(chunk: Chunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 1..127) {
					val block = chunk.getBlock(x, y, z)
					if (Util.binarySearch(block.type, ores)) {
						block.setType(Material.STONE, false)
					}
				}
			}
		}
	}
}