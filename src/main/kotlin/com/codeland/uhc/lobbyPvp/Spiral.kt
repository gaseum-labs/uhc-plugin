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

	/* internal */

	private fun currentAxis() = currentDir % 2
	private fun increment() = 1 - (currentDir / 2) * 2
}
