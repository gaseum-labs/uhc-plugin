package org.gaseumlabs.uhc.database.summary

import org.bukkit.Material

enum class GameType(val representation: Material, val color: Int) {
	UHC(Material.IRON_SWORD, 0xf0ca0e),
	CHC(Material.GOLDEN_SWORD, 0x0ed2f0);

	companion object {
		fun fromString(string: String): GameType {
			return values().find { it.name == string }
				?: throw Exception("game type \"${string}\" not recognized")
		}
	}
}
