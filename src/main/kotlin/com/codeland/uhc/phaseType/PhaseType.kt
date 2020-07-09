package com.codeland.uhc.phaseType;

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
}
