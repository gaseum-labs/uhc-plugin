package com.codeland.uhc.phaseType;

import com.codeland.uhc.gui.Gui
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.boss.BarColor
import java.util.logging.Level

enum class PhaseType(val prettyName: String, val hasTimer: Boolean, val color: BarColor) {
	WAITING("Waiting lobby", false, BarColor.WHITE),
	GRACE("Grace period", true, BarColor.BLUE),
	SHRINK("Shrinking period", true, BarColor.GREEN),
	FINAL("Final zone", true, BarColor.RED),
	GLOWING("Glowing period", true, BarColor.PURPLE),
	ENDGAME("Endgame", false, BarColor.YELLOW),
	POSTGAME("Postgame", false, BarColor.WHITE);
}
