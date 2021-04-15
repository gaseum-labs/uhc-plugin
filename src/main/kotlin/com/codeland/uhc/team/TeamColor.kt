package com.codeland.uhc.team

import net.kyori.adventure.text.format.TextColor
import kotlin.random.Random

class TeamColor(val subdivisions: Int) {
	val cube = Array(subdivisions * subdivisions * subdivisions) { defaultTaken(it) }

	val maxTeams = ((subdivisions * subdivisions * subdivisions) - subdivisions) / 2

	private fun defaultTaken(index: Int): Boolean {
		val (r, g, b) = rgbFromIndex(index)
		return r == g && g == b && b == r
	}

	private fun findOneColor(random: Random): Int? {
		val offset = random.nextInt(0, cube.size)

		for (i in cube.indices) {
			val index = (i + offset) % cube.size

			if (!cube[index]) {
				cube[index] = true
				return index
			}
		}

		return null
	}

	private fun rgbFromIndex(index: Int): Triple<Int, Int, Int> {
		return Triple(
			index % subdivisions,
			(index / subdivisions) % subdivisions,
			((index / subdivisions) / subdivisions) % subdivisions
		)
	}

	private fun colorFromRGB(rgb: Triple<Int, Int, Int>): TextColor {
		return TextColor.color(rgb.first.shl(16).or(rgb.second.shl(8)).or(rgb.third))
	}

	fun pickTeam(): Pair<TextColor, TextColor>? {
		val random = Random((Math.random() * Int.MAX_VALUE).toInt())

		val color1 = findOneColor(random) ?: return null
		val color2 = findOneColor(random) ?: return null

		return Pair(colorFromRGB(rgbFromIndex(color1)), colorFromRGB(rgbFromIndex(color2)))
	}

	private fun indexFromRgb(red: Int, gre: Int, blu: Int): Int {
		val x = red / (256 / subdivisions)
		val y = gre / (256 / subdivisions)
		val z = blu / (256 / subdivisions)

		return x + (y * subdivisions) + (z * subdivisions * subdivisions)
	}

	fun removeTeam(color0: TextColor, color1: TextColor) {
		cube[indexFromRgb(color0.red(), color0.green(), color0.blue())] = false
		cube[indexFromRgb(color1.red(), color1.green(), color1.blue())] = false
	}

	fun removeAllTeams() {
		for (i in cube.indices) cube[i] = defaultTaken(i)
	}
}
