package com.codeland.uhc.util

import com.codeland.uhc.extensions.MiscExtensions.nextFloat
import com.destroystokyo.paper.utils.PaperPluginLogger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.chat.*
import org.bukkit.ChatColor
import org.bukkit.World
import kotlin.math.acos
import kotlin.math.floor
import kotlin.math.sqrt

object Util {
	/**
	 * positive mod
	 */
	fun mod(a: Int, b: Int): Int {
		return ((a % b) + b) % b
	}

	fun mod(a: Float, b: Float): Float {
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
		return Random.nextInt(high + 1)
	}

	fun lowBiasRandom(high: Int): Int {
		return (Math.random().pow(3.0) * high).toInt()
	}

	fun randRange(low: Float, high: Float): Float {
		return Random.nextFloat(low, high)
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

	fun bilinear(array: Array<Float>, along: Float): Float {
		val x = along * array.size

		val weight1 = x - floor(x)
		val weight0 = 1 - weight1

		val left = mod(floor(x).toInt(), array.size)
		val right =  mod(left + 1, array.size)

		return array[left] * weight0 + array[right] + weight1
	}

	fun bilinear2D(array: Array<Float>, width: Int, height: Int, x: Float, y: Float): Float {
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

	/* the area of the intersection of two circles with the same radius at a given distance of centers */
	fun circleIntersection(r: Double, d: Double): Double {
		if (d > r * 2) return 0.0
		return 2 * (r * r * acos(d / (2 * r))) - 0.5 * sqrt((-d + 2 * r) * d * d * (d + 2 * r))
	}

	fun levelIntersection(r: Double, d: Double): Double {
		return (2 * r - d).coerceAtLeast(0.0)
	}

	fun interpColor(along: Float, from: TextColor, to: TextColor): TextColor {
		val red = ((  to.red() -   from.red()) * along +   from.red()).toInt()
		val gre = ((to.green() - from.green()) * along + from.green()).toInt()
		val blu = (( to.blue() -  from.blue()) * along +  from.blue()).toInt()
		return TextColor.color(red, gre, blu)
	}

	fun gradientString(string: String, from: TextColor, to: TextColor): Component {
		var component = Component.empty()

		string.forEachIndexed { i, c ->
			component = component.append(
				Component.text(c, interpColor(i.toFloat() / (string.length - 1), from, to))
			)
		}

		return component
	}

	fun nmsGradientString(string: String, from: TextColor, to: TextColor): IChatMutableComponent {
		var component = ChatComponentText("") as IChatMutableComponent

		string.forEachIndexed { i, c ->
			val along = i.toFloat() / (string.length - 1)
			val red = ((to.red() - from.red()) * along + from.red()).toInt()
			val gre = ((to.green() - from.green()) * along + from.green()).toInt()
			val blu = ((to.blue() - from.blue()) * along + from.blue()).toInt()
			component = component.addSibling(ChatComponentText("$c").setChatModifier(ChatModifier.a.setColor(ChatHexColor.a(red.shl(16).or(gre.shl(8)).or(blu)))))
		}

		return component
	}

	fun nmsGradientStringStylized(string: String, from: TextColor, to: TextColor, chatModifier: ChatModifier, exclude: Array<Int>): IChatMutableComponent {
		var component = ChatComponentText("") as IChatMutableComponent

		string.forEachIndexed { i, c ->
			val along = i.toFloat() / (string.length - 1)
			val red = ((to.red() - from.red()) * along + from.red()).toInt()
			val gre = ((to.green() - from.green()) * along + from.green()).toInt()
			val blu = ((to.blue() - from.blue()) * along + from.blue()).toInt()

			val modifier = (if (exclude.contains(i)) ChatModifier.a else chatModifier).setColor(ChatHexColor.a(red.shl(16).or(gre.shl(8)).or(blu)))
			component = component.addSibling(ChatComponentText("$c").setChatModifier(modifier))
		}

		return component
	}

	fun coloredInGameMessage(string: String, color: ChatColor): String {
		return "$color${ChatColor.BOLD}$string${ChatColor.GOLD}${ChatColor.BOLD}"
	}
}

fun main() {
	for (i in 0..90) {
		val (first, second) = Util.combination(i)
		val result = Util.inverseCombination(first, second)

		println("$i -> ($first, $second) -> $result")
	}
}
