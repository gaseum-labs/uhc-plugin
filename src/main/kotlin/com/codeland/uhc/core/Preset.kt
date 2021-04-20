package com.codeland.uhc.core

import com.codeland.uhc.util.Util
import org.bukkit.Material

enum class Preset(var prettyName: String, var startRadius: Int, var endRadius: Int, var graceTime: Int, var shrinkTime: Int, var representation: Material) {
	LARGE("Large", 500, 30, 1200, 3600, Material.ENCHANTING_TABLE),
	MEDIUM("Medium", 375, 30, 1200, 3000, Material.GOLDEN_AXE),
	TEST("Testing", 100, 30, 300, 300, Material.STRUCTURE_VOID);

	fun createLore(): List<String> {
		return createLore(startRadius, endRadius, graceTime, shrinkTime)
	}

	companion object {
		val NO_PRESET_REPRESENTATION = Material.END_CRYSTAL

		fun createLore(startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int): List<String> {
			return listOf(
				"Start radius: $startRadius",
				"End radius: $endRadius",
				"Grace time: ${Util.timeString(graceTime)}",
				"Shrink time: ${Util.timeString(shrinkTime)}"
			)
		}
	}
}
