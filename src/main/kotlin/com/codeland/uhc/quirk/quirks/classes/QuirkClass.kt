package com.codeland.uhc.quirk.quirks.classes

import org.bukkit.Material

enum class QuirkClass(val prettyName: String, val headBlock: Material) {
	NO_CLASS("", Material.DIRT),
	LAVACASTER("Lavacaster", Material.MAGMA_BLOCK),
	MINER("Miner", Material.DIAMOND_ORE),
	HUNTER("Hunter", Material.WITHER_SKELETON_SKULL),
	ALCHEMIST("Alchemist", Material.RED_STAINED_GLASS),
	ENCHANTER("Enchanter", Material.ENCHANTING_TABLE),
	DIVER("Diver", Material.PRISMARINE_BRICKS),
	TRAPPER("Trapper", Material.PISTON)
}
