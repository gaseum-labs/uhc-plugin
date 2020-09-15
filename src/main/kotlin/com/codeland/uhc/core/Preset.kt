package com.codeland.uhc.core

import com.codeland.uhc.util.Util
import org.bukkit.Material

enum class Preset(var prettyName: String, var startRadius: Double, var endRadius: Double, var graceTime: Int, var shrinkTime: Int, var finalTime: Int, var representation: Material) {
	LARGE("Large", 550.0, 25.0, 1200, 3000, 600, Material.ENCHANTING_TABLE),
	MEDIUM("Medium", 400.0, 25.0, 1200, 2000, 600, Material.GOLDEN_AXE),
	SMALL("Small", 200.0, 25.0, 900, 1800, 600, Material.STICK),
	TEST("Testing", 100.0, 25.0, 300, 300, 300, Material.STRUCTURE_VOID);

	fun createLore(): List<String> {
		return createLore(startRadius, endRadius, graceTime, shrinkTime, finalTime)
	}

	companion object {
		val NO_PRESET_REPRESENTATION = Material.END_CRYSTAL

		fun createLore(startRadius: Double, endRadius: Double, graceTime: Int, shrinkTime: Int, finalTime: Int): List<String> {
			return listOf(
				"Start radius: $startRadius",
				"End radius: $endRadius",
				"Grace time: ${Util.timeString(graceTime)}",
				"Shrink time: ${Util.timeString(shrinkTime)}",
				"Final time: ${Util.timeString(finalTime)}"
			)
		}
	}
}
