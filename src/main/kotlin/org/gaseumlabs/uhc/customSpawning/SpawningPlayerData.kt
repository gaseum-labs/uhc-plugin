package org.gaseumlabs.uhc.customSpawning

import kotlin.math.roundToInt

data class Count(
	var count: Int,
)

data class SpawningPlayerData(
	var index: Int,
	var cycle: Int,
	var cap: Double,
	var isAttempting: Boolean,
	val counts: HashMap<Class<*>, Count>,
) {
	constructor() : this(0, 0, 0.0, false, HashMap())

	fun intCap(): Int {
		return cap.roundToInt()
	}
}
