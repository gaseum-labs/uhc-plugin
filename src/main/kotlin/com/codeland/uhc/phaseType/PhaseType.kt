package com.codeland.uhc.phaseType;

import com.codeland.uhc.gui.Gui
import com.destroystokyo.paper.utils.PaperPluginLogger
import java.util.logging.Level

enum class PhaseType(prettyName: String, hasTimer: Boolean) {
	WAITING("Waiting lobby", false),
	GRACE("Grace period", true),
	SHRINK("Shrinking period", true),
	FINAL("Final zone", true),
	GLOWING("Glowing period", true),
	ENDGAME("Endgame", false),
	POSTGAME("Postgame", false);

	val prettyName = prettyName
	val hasTimer = hasTimer
	var time: Long? = 0L
	get() {
		if (!hasTimer)
			return null

		return field
	}

	var factory = null as PhaseFactory?
	set(value) {
		field = value

		if (field != null) {
			Gui.updatePhaseType(this)
		}
	}

	companion object {
		fun getFactory(phaseType: PhaseType): PhaseFactory {
			return values()[phaseType.ordinal].factory!!
		}

		fun getFactory(index: Int): PhaseFactory {
			return values()[index].factory!!
		}
	}
}
