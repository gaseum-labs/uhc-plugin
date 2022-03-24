package org.gaseumlabs.uhc.core.stats

import org.gaseumlabs.uhc.util.*
import org.bukkit.Material

enum class GameType(val representation: Material, val color: Int) {
	UHC(Material.IRON_SWORD, 0xf0ca0e),
	CHC(Material.GOLDEN_SWORD, 0x0ed2f0);

	companion object {
		fun fromString(string: String): Result<GameType> {
			return Good(
				values().find { it.name == string }
					?: return Bad("game type \"${string}\" not recognized")
			)
		}
	}
}
