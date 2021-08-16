package com.codeland.uhc.lobbyPvp

import kotlin.math.abs

class Spiral {
	var currentDir = 0

	val axes = arrayOf(0, 0)
	/* positive x, positive z, negative x, negative z */
	val maxes = arrayOf(0, 0, 0, 0)

	fun getX(): Int {
		return axes[0]
	}

	fun getZ(): Int {
		return axes[1]
	}

	fun maxX(): Int {
		return maxes[0]
	}

	fun maxZ(): Int {
		return maxes[1]
	}

	fun minX(): Int {
		return -maxes[2]
	}

	fun minZ(): Int {
		return -maxes[3]
	}

	fun next() {
		axes[currentAxis()] += increment()

		if (abs(axes[currentAxis()]) > maxes[currentDir]) {
			maxes[currentDir] = abs(axes[currentAxis()])
			currentDir = (currentDir + 1) % 4
		}
	}

	fun toMetadata(): String {
		return "${axes[0]},${axes[1]},${maxes[0]},${maxes[1]},${maxes[2]},${maxes[3]}"
	}

	fun fromMetadata(metadata: String): Boolean {
		val parts = metadata.split(',')
		if (parts.size != 6) return false

		val x = parts[0].toIntOrNull() ?: return false
		val z = parts[1].toIntOrNull() ?: return false
		val maxX = parts[2].toIntOrNull() ?: return false
		val maxZ = parts[3].toIntOrNull() ?: return false
		val minX = parts[4].toIntOrNull() ?: return false
		val minZ = parts[5].toIntOrNull() ?: return false

		axes[0] = x
		axes[1] = z
		maxes[0] = maxX
		maxes[1] = maxZ
		maxes[2] = minX
		maxes[3] = minZ

		return true
	}

	/* internal */

	private fun currentAxis() = currentDir % 2
	private fun increment() = 1 - (currentDir / 2) * 2
}
