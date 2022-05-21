package org.gaseumlabs.uhc.team

import net.kyori.adventure.text.format.TextColor
import kotlin.random.Random

class ColorCube(val subdivisions: Int) {
	private val cube = Array(subdivisions * subdivisions * subdivisions) { defaultTaken(it) }

	private val subSize = 256 / subdivisions

	private fun defaultTaken(index: Int): Boolean {
		val (x, y, z) = positionFromIndex(index)
		return x == y && y == z && z == x
	}

	private fun findIndex(random: Random): Int? {
		val offset = random.nextInt(0, cube.size)

		for (i in cube.indices) {
			val index = (i + offset) % cube.size
			if (!cube[index]) return index
		}

		return null
	}

	/* position -> index -> color conversion */

	fun positionFromIndex(index: Int): Triple<Int, Int, Int> {
		return Triple(
			index % subdivisions,
			(index / subdivisions) % subdivisions,
			((index / subdivisions) / subdivisions) % subdivisions
		)
	}

	fun indexFromPosition(x: Int, y: Int, z: Int): Int {
		return x + (y * subdivisions) + (z * subdivisions * subdivisions)
	}

	fun colorFromIndex(index: Int): TextColor {
		return colorFromPosition(positionFromIndex(index))
	}

	fun colorFromPosition(x: Int, y: Int, z: Int): TextColor {
		return TextColor.color(
			(x * subSize) + (subSize / 2),
			(y * subSize) + (subSize / 2),
			(z * subSize) + (subSize / 2)
		)
	}

	fun indexFromColor(red: Int, gre: Int, blu: Int): Int {
		return indexFromPosition(
			red / (256 / subdivisions),
			gre / (256 / subdivisions),
			blu / (256 / subdivisions)
		)
	}

	fun colorFromPosition(xyz: Triple<Int, Int, Int>): TextColor {
		return colorFromPosition(xyz.first, xyz.second, xyz.third)
	}

	fun indexFromColor(color: TextColor): Int {
		return indexFromColor(color.red(), color.green(), color.blue())
	}

	/* team interface */

	/**
	 * picks two free colors for a new teams and modifies
	 * internal cube so that they are marked as taken
	 *
	 * @return the two team colors, or null if no team could be created
	 */
	fun pickTeam(): Pair<TextColor, TextColor>? {
		val random = Random((Math.random() * Int.MAX_VALUE).toInt())

		val index0 = findIndex(random) ?: return null
		val index1 = findIndex(random) ?: return null

		cube[index0] = true
		cube[index1] = true

		return Pair(colorFromPosition(positionFromIndex(index0)), colorFromPosition(positionFromIndex(index1)))
	}

	/**
	 * unmarks the colors as taken from the internal cube
	 */
	fun removeTeam(colors: Array<TextColor>) {
		colors.forEach { color ->
			cube[indexFromColor(color.red(), color.green(), color.blue())] = false
		}
	}

	fun switchColor(from: Int, to: Int) {
		cube[from] = false
		cube[to] = true
	}

	fun taken(r: Int, g: Int, b: Int): Boolean {
		return cube[indexFromPosition(r, g, b)]
	}

	fun taken(index: Int): Boolean {
		return cube[index]
	}
}
