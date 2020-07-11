package com.codeland.uhc.phaseType;

import com.codeland.uhc.gui.Gui
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.boss.BarColor
import java.util.logging.Level

enum class PhaseType(var prettyName: String, var hasTimer: Boolean, var color: BarColor) {
	WAITING("Waiting lobby", false, BarColor.WHITE),
	GRACE("Grace period", true, BarColor.BLUE),
	SHRINK("Shrinking period", true, BarColor.GREEN),
	FINAL("Final zone", true, BarColor.RED),
	GLOWING("Glowing period", true, BarColor.PURPLE),
	ENDGAME("Endgame", false, BarColor.YELLOW),
	POSTGAME("Postgame", false, BarColor.WHITE);

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
