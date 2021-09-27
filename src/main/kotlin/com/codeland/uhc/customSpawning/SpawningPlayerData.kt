package com.codeland.uhc.customSpawning

import kotlin.math.roundToInt

data class SpawningPlayerData(
	var index: Int,
	var cycle: Int,
	var cap: Double,
	var isAttempting: Boolean,
) {
	constructor() : this(0, 0, 0.0, false)

	fun intCap(): Int {
		return cap.roundToInt()
	}
}
