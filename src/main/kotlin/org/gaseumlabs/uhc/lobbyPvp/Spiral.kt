package org.gaseumlabs.uhc.lobbyPvp

import org.gaseumlabs.uhc.util.KeyGen
import org.gaseumlabs.uhc.util.createComplexDataType
import kotlin.math.abs

/**
 * directions, also indices of maxes:
 *  0: positive x
 *  1: positive z
 *  2: negative x
 *  3: negative z
 *
 * position:
 *  [0]: x
 *  [1]: z
 */
class Spiral(
	private var currentDir: Int,
	private val position: IntArray,
	private val extremities: IntArray
) {
	fun currentDir() = currentDir
	fun getX() = position[0]
	fun getZ() = position[1]

	fun maxX() = extremities[0]
	fun maxZ() = extremities[1]
	fun minX() = -extremities[2]
	fun minZ() = -extremities[3]

	fun next() {
		position[currentAxis()] += increment()

		if (abs(position[currentAxis()]) > extremities[currentDir]) {
			extremities[currentDir] = abs(position[currentAxis()])
			currentDir = (currentDir + 1) % 4
		}
	}

	private fun currentAxis() = currentDir % 2
	private fun increment() = 1 - (currentDir / 2) * 2

	companion object {
		fun defaultSpiral() = Spiral(0, intArrayOf(0, 0), intArrayOf(0, 0, 0, 0))

		val key = KeyGen.genKey("pvp_spiral")

		val spiralData = createComplexDataType(
			7 * 4,
			{ spiral, buffer ->
				buffer.putInt(spiral.currentDir())

				buffer.putInt(spiral.getX())
				buffer.putInt(spiral.getZ())

				buffer.putInt(spiral.maxX())
				buffer.putInt(spiral.maxZ())
				buffer.putInt(spiral.minX())
				buffer.putInt(spiral.minZ())
			}, { buffer ->
				Spiral(
					buffer.int,
					intArrayOf(buffer.int, buffer.int),
					intArrayOf(buffer.int, buffer.int, buffer.int, buffer.int)
				)
			}
		)
	}
}
