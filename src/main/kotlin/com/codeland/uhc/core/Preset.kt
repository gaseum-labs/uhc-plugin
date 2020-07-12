package com.codeland.uhc.core

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class Preset(var prettyName: String, var startRadius: Double, var endRadius: Double, var graceTime: Long, var shrinkTime: Long, var finalTime: Long, var glowTime: Long, var representation: Material) {
	STANDARD("Standard", 400.0, 25.0, 1200, 2250, 600, 600, Material.GOLDEN_AXE),
	SMALL("Small", 200.0, 25.0, 900, 1600, 600, 600, Material.STICK),
	TEST("Testing", 100.0, 25.0, 300, 300, 300, 300, Material.STRUCTURE_VOID);

	fun createLore(): List<String> {
		return listOf(
			"Start radius: $startRadius",
			"End radius: $endRadius",
			"Grace time: $graceTime",
			"Shrink time: $shrinkTime",
			"Final time: $finalTime",
			"Glow time: $glowTime"
		)
	}
}
