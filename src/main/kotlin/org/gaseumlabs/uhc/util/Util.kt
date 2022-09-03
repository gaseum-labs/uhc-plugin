package org.gaseumlabs.uhc.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.chat.*
import org.bukkit.*
import java.util.logging.*
import kotlin.math.*

object Util {
	inline fun Any?.unit() = Unit
	inline fun Any?.void() = null
	inline fun <V>Any?.comma(v: V) = v
	inline fun trueThrough(value: Boolean, onTrue: () -> Unit)
		= if (value) onTrue().comma(true) else false
	inline fun <T> T?.and(check: () -> Boolean) = if (check()) this else null

	/**
	 * positive mod
	 */
	fun mod(a: Int, b: Int): Int {
		return ((a % b) + b) % b
	}

	fun mod(a: Float, b: Float): Float {
		return ((a % b) + b) % b
	}

	fun coordPack(x: Int, z: Int, seed: Long): Long {
		return x.toLong().shl(32).or(z.toLong().and(0xffff)).xor(seed)
	}

	fun topBlockY(world: World, x: Int, z: Int): Int {
		for (y in 255 downTo 0) {
			if (!world.getBlockAt(x, y, z).isPassable) return y
		}

		return 0
	}

	fun topLiquidSolidY(world: World, x: Int, z: Int): Pair<Int, Int> {
		for (y in 255 downTo 0) {
			val block = world.getBlockAt(x, y, z)

			if (block.isLiquid) {
				return Pair(y, -1)
			}

			if (!block.isPassable) {
				return Pair(-1, y)
			}
		}

		return Pair(-1, -1)
	}

	inline fun <T, E : Enum<E>> binaryFind(value: E, array: Array<T>, getValue: (T) -> E): T? {
		var start = 0
		var end = array.size

		while (true) {
			val position = (end + start) / 2
			val currentValue = getValue(array[position])

			when {
				currentValue === value -> return array[position]
				end - start == 1 -> return null
				value < currentValue -> end = position
				value > currentValue -> start = position
			}
		}
	}

	inline fun <T, E : Enum<E>> binarySearch(value: E, array: Array<T>, getValue: (T) -> E): Boolean {
		return binaryFind(value, array, getValue) != null
	}

	inline fun <T : Enum<T>> binarySearch(value: T, array: Array<T>): Boolean {
		return binaryFind(value, array) { it } != null
	}

	fun timeString(seconds: Int): String {
		val minutes = seconds / 60
		val seconds = seconds % 60

		val minutesPart = if (minutes == 0) "" else "$minutes minute${if (minutes == 1) "" else "s"}"
		val secondsPart = if (seconds == 0) "" else "$seconds second${if (seconds == 1) "" else "s"}"

		return "$minutesPart${if (minutesPart == "") "" else " "}$secondsPart"
	}

	fun interp(low: Float, high: Float, along: Float): Float {
		return (high - low) * along + low
	}

	fun interp(low: Double, high: Double, along: Double): Double {
		return (high - low) * along + low
	}

	fun interpClamp(low: Float, high: Float, along: Float): Float {
		val value = (high - low) * along + low

		return when {
			value < low -> low
			value > high -> high
			else -> value
		}
	}

	fun invInterp(low: Float, high: Float, value: Float): Float {
		return (value - low) / (high - low)
	}

	fun invInterp(low: Double, high: Double, value: Double): Double {
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
		val right = mod(left + 1, array.size)

		return array[left] * weight0 + array[right] + weight1
	}

	fun bilinear2D(array: Array<Float>, width: Int, height: Int, x: Float, y: Float): Float {
		val minX = (x * width).toInt() % width
		val maxX = (minX + 1) % width

		val coefX = x - minX

		val minY = (y * height).toInt() % height
		val maxY = (minY + 1) % height

		val coefY = y - minY

		return (array[minY * width + minX] * coefY * coefX) +
		(array[minY * width + maxX] * coefY * (1 - coefX)) +
		(array[maxY * width + minX] * (1 - coefY) * coefX) +
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

	fun interpColor(along: Float, from: Color, to: Color): TextColor {
		val red = ((to.red - from.red) * along + from.red).toInt()
		val gre = ((to.green - from.green) * along + from.green).toInt()
		val blu = ((to.blue - from.blue) * along + from.blue).toInt()
		return TextColor.color(red, gre, blu)
	}

	fun interpColor(along: Float, from: TextColor, to: TextColor): TextColor {
		val red = ((to.red() - from.red()) * along + from.red()).toInt()
		val gre = ((to.green() - from.green()) * along + from.green()).toInt()
		val blu = ((to.blue() - from.blue()) * along + from.blue()).toInt()
		return TextColor.color(red, gre, blu)
	}

	fun gradientString(string: String, from: DyeColor, to: DyeColor): TextComponent {
		var component = Component.empty()

		string.forEachIndexed { i, c ->
			component = component.append(
				Component.text(c, interpColor(i.toFloat() / (string.length - 1), from.color, to.color))
			)
		}

		return component
	}

	fun gradientString(string: String, from: TextColor, to: TextColor): TextComponent {
		var component = Component.empty()

		string.forEachIndexed { i, c ->
			component = component.append(
				Component.text(c, interpColor(i.toFloat() / (string.length - 1), from, to))
			)
		}

		return component
	}

	fun nmsGradientString(string: String, from: Int, to: Int): MutableComponent {
		var component = TextComponent("") as MutableComponent

		string.forEachIndexed { i, c ->
			val along = i.toFloat() / (string.length - 1)
			val red = (((to.shr(16)) - from.shr(16)) * along + from.shr(16)).toInt()
			val gre = ((to.shr(8).and(0xff) - from.shr(8).and(0xff)) * along + from.shr(8).and(0xff)).toInt()
			val blu = ((to.and(0xff) - from.and(0xff)) * along + from.and(0xff)).toInt()

			component = component.append(
				TextComponent("$c").setStyle(Style.EMPTY.withColor(
					red.shl(16).or(gre.shl(8)).or(blu))
				)
			)
		}

		return component
	}

	fun materialRange(a: Material, b: Material): List<Material> {
		return (a.ordinal..b.ordinal).toList().map(Material.values()::get)
	}

	fun <B> fieldError(name: String, type: String): Bad<B> {
		return Bad("No value for \"${name}\" <${type}> found")
	}

	fun floorDiv(a: Int, b: Int) = if (a < 0) (a - b + 1) / b else a / b

	//fun atLeastOf(required: Int, vararg tries: Boolean): Boolean {
	//	//var count = 0
	//	//for (t in tries) {
	//	//	if (t && ++count == tries) return true
	//	//}
	//	//return false
	//}

	fun <T> T.takeFrom(expr: Boolean): T? {
		return if (expr) this else null
	}

	private val logger = Bukkit.getLogger()

	fun log(message: String) {
		logger.log(Level.INFO, message)
	}

	fun warn(message: String) {
		logger.log(Level.WARNING, message)
	}

	fun warn(exception: Throwable) {
		logger.log(Level.WARNING, exception.localizedMessage)
	}

	inline fun <reified T : Enum<T>> sortedArrayOf(vararg array: T): Array<T> {
		array.sort()
		return array as Array<T>
	}

	inline fun <reified R, reified T : Enum<T>>sortedArrayOf(
		vararg array: R,
		crossinline mapping: (R) -> T
	): Array<R> {
		array.sortBy { mapping(it) }
		return array as Array<R>
	}
}
