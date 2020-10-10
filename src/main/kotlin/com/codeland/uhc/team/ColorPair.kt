package com.codeland.uhc.team

import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.Color
import kotlin.math.ceil

class ColorPair {
	constructor(color0: ChatColor, color1: ChatColor?) {
		this.color0 = color0
		this.color1 = color1
	}

	constructor(color: ChatColor) {
		this.color0 = color
		this.color1 = null
	}

	val color0: ChatColor
	val color1: ChatColor?

	/**
	 * colors a string based on this colorPair
	 * single colored pairs will just make the string one color
	 * double colored pairs will alternate colors for each character in the string
	 *
	 * @return a new string that is colored
	 */
	fun colorStringAlternating(string: String): String {
		return if (color1 == null) {
			"$color0$string"
		} else {
			val byteArray = CharArray(string.length * 3)

			for (i in string.indices) {
				byteArray[i * 3] = ChatColor.COLOR_CHAR
				byteArray[i * 3 + 1] = if (i % 2 == 0) color0.char else color1.char
				byteArray[i * 3 + 2] = string[i]
			}

			String(byteArray)
		}
	}

	/**
	 * colors a string based on this colorPair
	 * single colored pairs will just make the string one color
	 * double colored pairs will set color halves of the string
	 *
	 * @return a new string that is colored
	 */
	fun colorString(string: String): String {
		return if (color1 == null) {
			"$color0$string"
		} else {
			val half = ceil(string.length / 2.0).toInt()

			"$color0${string.substring(0, half)}$color1${string.substring(half)}"
		}
	}

	/**
	 * colors a string based on this colorPair
	 * also adds a modifier for each color
	 * single colored pairs will just make the string one color
	 * double colored pairs will alternate colors for each character in the string
	 *
	 * @return a new string that is colored
	 */
	fun colorStringModifiedAlternating(string: String, modifier: ChatColor): String {
		return if (color1 == null) {
			"$color0$modifier$string"
		} else {
			val byteArray = CharArray(string.length * 5)

			for (i in string.indices) {
				byteArray[i * 5] = ChatColor.COLOR_CHAR
				byteArray[i * 5 + 1] = if (i % 2 == 0) color0.char else color1.char
				byteArray[i * 5 + 2] = ChatColor.COLOR_CHAR
				byteArray[i * 5 + 3] = modifier.char
				byteArray[i * 5 + 4] = string[i]
			}

			String(byteArray)
		}
	}

	/**
	 * colors a string based on this colorPair
	 * also adds a modifier for each color
	 * single colored pairs will just make the string one color
	 * double colored pairs will set color halves of the string
	 *
	 * @return a new string that is colored
	 */
	fun colorStringModified(string: String, modifier: ChatColor): String {
		return if (color1 == null) {
			"$color0$modifier$string"
		} else {
			val half = ceil(string.length / 2.0).toInt()

			"$color0$modifier${string.substring(0, half)}$color1$modifier${string.substring(half)}"
		}
	}

	fun getName(): String {
		return "${Util.colorPrettyNames[color0.ordinal]}${if (color1 == null) "" else " ${Util.colorPrettyNames[color1.ordinal]}"}"
	}

	override fun equals(other: Any?): Boolean {
		if (other !is ColorPair) return false

		return (color0 == other.color0 || color0 == other.color1) && (color1 == other.color0 || color1 == other.color1)
	}

	companion object {
		val DEFAULT = ColorPair(ChatColor.WHITE)
	}
}
