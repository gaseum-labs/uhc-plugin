package com.codeland.uhc.util

import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.World
import java.util.logging.Level
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

object Util {
	fun log(message: String) {
		PaperPluginLogger.getGlobal().log(Level.INFO, message)
	}

	fun debug(message: String) {
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

	fun topBlockYTop(world: World, top: Int, x: Int, z: Int): Int {
		for (y in top downTo 0) {
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

	fun topLiquidSolidYTop(world: World, top: Int, x: Int, z: Int): Pair<Int, Int> {
		for (y in top downTo 0) {
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

	fun lowBiasRandom(high: Int): Int {
		return (Math.random().pow(3.0) * high).toInt()
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

	fun interp(low: Float, high: Float, along: Float): Float {
		return (high - low) * along + low
	}

	fun interpClamp(low: Float, high: Float, along: Float): Float {
		var value = (high - low) * along + low

		return when {
			value < low -> low
			value > high -> high
			else -> value
		}
	}

	fun invInterp(low: Float, high: Float, value: Float): Float {
		return (value - low) / (high - low)
	}

	fun combination(index: Int): Pair<Int, Int> {
		val first = inverseSumToN(index) - 1
		return Pair(first, index - sumToN(first))
	}

	fun inverseCombination(first: Int, second: Int) = sumToN(first) + second

	fun sumToN(n: Int) = (n * (n + 1)) / 2

	fun inverseSumToN(sum: Int) = ((1 + sqrt(1 + 8.0 * sum)) / 2).toInt()

	val colorPrettyNames = arrayOf(
		"Black",
		"Dark Blue",
		"Dark Green",
		"Dark Aqua",
		"Dark Red",
		"Dark Purple",
		"Gold",
		"Gray",
		"Dark Gray",
		"Blue",
		"Green",
		"Aqua",
		"Red",
		"Light Purple",
		"Yellow",
		"White",
		"Magic",
		"Bold",
		"Strike",
		"Underline",
		"Italic",
		"Reset"
	)

	val worldPrettyNames = arrayOf(
		"Overworld",
		"Nether",
		"End"
	)

	fun bilinearWrap(array: Array<Float>, width: Int, height: Int, x: Float, y: Float): Float {
		val minX = (x * width).toInt() % width
		val maxX = (minX + 1) % width

		val coefX = x - minX

		val minY = (y * height).toInt() % height
		val maxY = (minY + 1) % height

		val coefY = y - minY

		return (array[minY * width + minX] *      coefY  *      coefX ) +
			   (array[minY * width + maxX] *      coefY  * (1 - coefX)) +
			   (array[maxY * width + minX] * (1 - coefY) *      coefX ) +
			   (array[maxY * width + maxX] * (1 - coefY) * (1 - coefX))
	}

	fun <T>pickTwo(array: Array<T>): Pair<T, T> {
		var index0 = (Math.random() * array.size).toInt()
		val index1 = (Math.random() * array.size).toInt()

		if (index0 == index1) index0 = (index0 + 1) % array.size

		return Pair(array[index0], array[index1])
	}

	fun <T>swap(array: Array<T>, index0: Int, index1: Int) {
		val temp = array[index0]
		array[index0] = array[index1]
		array[index1] = temp
	}

	/* the area of the intersection of two circles with the same radius at a given distance of centers */
	fun circleIntersection(r: Double, d: Double): Double {
		if (d > r * 2) return 0.0
		return 2 * (r * r * acos(d / (2 * r))) - 0.5 * sqrt((-d + 2 * r) * d * d * (d + 2 * r))
	}

	fun levelIntersection(r: Double, d: Double): Double {
		return (2 * r - d).coerceAtLeast(0.0)
	}

	fun <T>shuffleArray(array: Array<T>): Array<T> {
		val random = Random((Math.random() * 7238201).toInt())

		for (i in array.indices) {
			val otherIndex = random.nextInt(0, array.size)

			val temp = array[i]
			array[i] = array[otherIndex]
			array[otherIndex] = temp
		}

		return array
	}
}

fun main() {
	for (i in 0..90) {
		val (first, second) = Util.combination(i)
		val result = Util.inverseCombination(first, second)

		println("$i -> ($first, $second) -> $result")
	}
}
