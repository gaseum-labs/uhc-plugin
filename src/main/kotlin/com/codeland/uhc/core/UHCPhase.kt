package com.codeland.uhc.core

enum class UHCPhase {
	WAITING,
	GRACE,
	SHRINKING,//includes time before glowing
	GLOWING,
	POSTGAME,
}
