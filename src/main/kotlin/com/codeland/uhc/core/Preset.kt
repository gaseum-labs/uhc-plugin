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
			"Grace time: ${prettyTime(graceTime)}",
			"Shrink time: ${prettyTime(shrinkTime)}",
			"Final time: ${prettyTime(finalTime)}",
			"Glow time: ${prettyTime(glowTime)}"
		)
	}

	fun prettyTime(time: Long): String {
		val minutes = time / 60
		val seconds = time % 60

		var minutesPart = if (minutes == 0L)
			""
		else
			"$minutes minute${if (minutes == 1L) "" else "s"}"

		var secondsPart = if (seconds == 0L)
			""
		else
			"$seconds second${if (seconds == 1L) "" else "s"}"

		return "$minutesPart $secondsPart"
	}
}
