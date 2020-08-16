package com.codeland.uhc.util

import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.World
import java.util.logging.Level

object Util {
	fun log(message: String) {
		PaperPluginLogger.getGlobal().log(Level.INFO, message)
	}

	fun topBlockY(world: World, x: Int, z: Int): Int {
		for (y in 255 downTo 0) {
			var block = world.getBlockAt(x, y, z)

			if (!block.isPassable)
				return y
		}

		return 0
	}

	fun <T : Enum<T>> binarySearch(value: T, array: Array<T>): Boolean {
		var start = 0
		var end = array.size - 1
		var lookFor = value.ordinal

		while (true) {
			var position = (end + start) / 2
			var compare = array[position].ordinal

			when {
				lookFor == compare -> return true
				end - start == 1 -> return false
				lookFor < compare -> end = position
				lookFor > compare -> start = position
			}
		}
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