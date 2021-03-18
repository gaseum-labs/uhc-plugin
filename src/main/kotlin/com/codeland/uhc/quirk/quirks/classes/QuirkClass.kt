package com.codeland.uhc.quirk.quirks.classes

import org.bukkit.Material

enum class QuirkClass(val prettyName: String, val headBlock: Material) {
	NO_CLASS("", Material.DIRT),
	LAVACASTER("lavacaster", Material.MAGMA_BLOCK),
	MINER("miner", Material.DIAMOND_ORE),
	HUNTER("hunter", Material.WITHER_SKELETON_SKULL),
	BREWER("brewer", Material.RED_STAINED_GLASS),
	ENCHANTER("enchanter", Material.ENCHANTING_TABLE)
}
