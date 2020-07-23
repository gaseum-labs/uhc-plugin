package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.phaseType.PhaseVariant

abstract class Quirk(var type: QuirkType) {
	/* default value will be set upon init */
	var enabled = type.defaultEnabled
	set(value) {
		/* enable / disable functions come first */
		if (value) {
			onEnable()
		} else {
			onDisable()
		}

		field = value
		Gui.updateQuirk(type)

		GameRunner.log("${type.prettyName} enabled is set to $enabled")

		type.incompatibilities.forEach { other ->
			var otherQuirk = GameRunner.uhc.getQuirk(other)

			if (otherQuirk.enabled) {
				otherQuirk.enabled = false
				Gui.updateQuirk(other)
			}
		}
	}

	abstract fun onEnable()
	abstract fun onDisable()

	open fun onPhaseSwitch(phase: PhaseVariant) {}
}