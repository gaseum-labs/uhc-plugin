package com.codeland.uhc.util

import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.World
import java.util.logging.Level

object Util {
	fun log(message: String) {
		PaperPluginLogger.getGlobal().log(Level.INFO, message)
	}

	/**
	 * positive mod
	 */
	fun mod(a: Int, b: Int): Int {
		return ((a % b) + b) % b
	}

	fun topBlockY(world: World, x: Int, z: Int): Int {
		for (y in 255 downTo 0) {
			var block = world.getBlockAt(x, y, z)

			if (!block.isPassable)
				return y
		}

		return 0
	}

	fun topLiquidSolidY(world: World, x: Int, z: Int): Pair<Int, Int> {
		for (y in 255 downTo 0) {
			var block = world.getBlockAt(x, y, z)

			if (block.isLiquid) {
				return Pair(y, -1)
			}

			if (!block.isPassable) {
				return Pair(-1, y)
			}
		}

		return Pair(-1, -1)
	}

	fun <T, E : Enum<E>> binaryFind(value: E, array: Array<T>, getValue: (T) -> E): T? {
		var start = 0
		var end = array.size

		while (true) {
			val position = (end + start) / 2
			val currentValue = getValue(array[position])

			when {
				currentValue == value -> return array[position]
				end - start == 1 -> return null
				value < currentValue -> end = position
				value > currentValue -> start = position
			}
		}
	}

	fun <T, E : Enum<E>> binarySearch(value: E, array: Array<T>, getValue: (T) -> E): Boolean {
		return binaryFind(value, array, getValue) != null
	}

	fun <T : Enum<T>> binarySearch(value: T, array: Array<T>): Boolean {
		return binaryFind(value, array, { item -> item }) != null
	}

	fun randRange(low: Int, high: Int): Int {
		return ((Math.random() * (high - low + 1)) + low).toInt()
	}

	fun randRange(low: Float, high: Float): Float {
		return ((Math.random() * (high - low)) + low).toFloat()
	}

	fun timeString(seconds: Int): String {
		val minutes = seconds / 60
		val seconds = seconds % 60

		var minutesPart = if (minutes == 0)
			""
		else
			"$minutes minute${if (minutes == 1) "" else "s"}"

		var secondsPart = if (seconds == 0)
			""
		else
			"$seconds second${if (seconds == 1) "" else "s"}"

		return "$minutesPart${if(minutesPart == "") "" else " "}$secondsPart"
	}

	fun <T>randFromArray(array: Array<T>): T {
		return array[(Math.random() * array.size).toInt()]
	}
}