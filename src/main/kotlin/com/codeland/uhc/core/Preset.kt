package com.codeland.uhc.core

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class Preset(var startRadius: Double, var endRadius: Double, var graceTime: Long, var shrinkTime: Long, var finalTime: Long, var glowTime: Long, representation: Material) {
	STANDARD(400.0, 25.0, 1200, 2250, 600, 600, Material.GOLDEN_AXE),
	SMALL(200.0, 25.0, 900, 1600, 600, 600, Material.STICK),
	TEST(100.0, 25.0, 300, 300, 300, 300, Material.STRUCTURE_VOID)
}
