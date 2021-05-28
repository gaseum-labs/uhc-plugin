package com.codeland.uhc.core

import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import org.bukkit.Material

class Setup(val prettyName: String, val representation: Material, val startRadius: Int, val endRadius: Int, val graceTime: Int, val shrinkTime: Int) {
	fun createLore(): List<Component> {
		return createLore(startRadius, endRadius, graceTime, shrinkTime)
	}

	fun phaseTime(phaseType: PhaseType): Int {
		return when (phaseType) {
			PhaseType.GRACE -> graceTime
			PhaseType.SHRINK -> shrinkTime
			else -> 0
		}
	}

	fun withStartRadius(newStartRadius: Int): Setup {
		return custom(newStartRadius, endRadius, graceTime, shrinkTime)
	}

	fun withEndRadius(newEndRadius: Int): Setup {
		return custom(startRadius, newEndRadius, graceTime, shrinkTime)
	}

	fun withGraceTime(newGraceTime: Int): Setup {
		return custom(startRadius, endRadius, newGraceTime, shrinkTime)
	}

	fun withShrinkTime(newShrinkTime: Int): Setup {
		return custom(startRadius, endRadius, graceTime, newShrinkTime)
	}

	fun withPhaseTime(phaseType: PhaseType, time: Int): Setup {
		return when (phaseType) {
			PhaseType.GRACE -> custom(startRadius, endRadius, time, shrinkTime)
			PhaseType.SHRINK -> custom(startRadius, endRadius, graceTime, time)
			else -> this
		}
	}

	companion object {
		val CUSTOM_REPRESENTATION = Material.END_CRYSTAL
		val CUSTOM_NAME = "Custom"

		fun createLore(startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int): List<Component> {
			return listOf(
				Component.text("Start radius: $startRadius"),
				Component.text("End radius: $endRadius"),
				Component.text("Grace time: ${Util.timeString(graceTime)}"),
				Component.text("Shrink time: ${Util.timeString(shrinkTime)}")
			)
		}

		fun custom(startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int): Setup {
			return Setup(CUSTOM_NAME, CUSTOM_REPRESENTATION, startRadius, endRadius, graceTime, shrinkTime)
		}
	}
}
