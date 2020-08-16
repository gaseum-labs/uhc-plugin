package com.codeland.uhc.core

import com.codeland.uhc.util.Util
import org.bukkit.Material

enum class Preset(var prettyName: String, var startRadius: Double, var endRadius: Double, var graceTime: Int, var shrinkTime: Int, var finalTime: Int, var glowTime: Int, var representation: Material) {
	LARGE("Large", 550.0, 25.0, 1200, 2250, 300, 300, Material.ENCHANTING_TABLE),
	MEDIUM("Medium", 400.0, 25.0, 1200, 2250, 300, 300, Material.GOLDEN_AXE),
	SMALL("Small", 200.0, 25.0, 900, 1600, 300, 300, Material.STICK),
	TEST("Testing", 100.0, 25.0, 300, 300, 300, 300, Material.STRUCTURE_VOID);

	fun createLore(): List<String> {
		return listOf(
			"Start radius: $startRadius",
			"End radius: $endRadius",
			"Grace time: ${Util.timeString(graceTime)}",
			"Shrink time: ${Util.timeString(shrinkTime)}",
			"Final time: ${Util.timeString(finalTime)}",
			"Glow time: ${Util.timeString(glowTime)}"
		)
	}
}
