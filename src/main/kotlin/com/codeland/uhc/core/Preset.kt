package com.codeland.uhc.core

import org.bukkit.Material

enum class Preset(val setup: Setup) {
	LARGE(Setup("Large",  Material.ENCHANTING_TABLE, 500, 30, 1200, 3600)),
	MEDIUM(Setup("Medium", Material.GOLDEN_AXE, 375, 30, 1200, 3000)),
	TEST(Setup("Testing", Material.STRUCTURE_VOID, 100, 30, 300, 300));

	companion object {
		fun findPreset(setup: Setup): Preset? {
			return values().find { it.setup.prettyName == setup.prettyName }
		}
	}
}
