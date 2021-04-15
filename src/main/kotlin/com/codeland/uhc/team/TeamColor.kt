package com.codeland.uhc.team

import net.kyori.adventure.text.format.TextColor
import kotlin.random.Random

class TeamColor(val subdivisions: Int) {
	/* may not actually, be used, instead relying solely on pickTeam for fail state */
	val maxTeams = ((subdivisions * subdivisions * subdivisions) - subdivisions) / 2

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

	private fun positionFromIndex(index: Int): Triple<Int, Int, Int> {
		return Triple(
			index % subdivisions,
			(index / subdivisions) % subdivisions,
			((index / subdivisions) / subdivisions) % subdivisions
		)
	}

	private fun colorFromPosition(xyz: Triple<Int, Int, Int>): TextColor {
		val random = Random((Math.random() * Int.MAX_VALUE).toInt())
		val red = xyz.first * subSize + random.nextInt(0, subSize)
		val gre = xyz.second * subSize + random.nextInt(0, subSize)
		val blu = xyz.third * subSize + random.nextInt(0, subSize)

		return TextColor.color(red.shl(16).or(gre.shl(8)).or(blu))
	}

	private fun indexFromColor(red: Int, gre: Int, blu: Int): Int {
		val x = red / (256 / subdivisions)
		val y = gre / (256 / subdivisions)
		val z = blu / (256 / subdivisions)

		return x + (y * subdivisions) + (z * subdivisions * subdivisions)
	}

	/* interface */

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
	fun removeTeam(color0: TextColor, color1: TextColor) {
		cube[indexFromColor(color0.red(), color0.green(), color0.blue())] = false
		cube[indexFromColor(color1.red(), color1.green(), color1.blue())] = false
	}
}
