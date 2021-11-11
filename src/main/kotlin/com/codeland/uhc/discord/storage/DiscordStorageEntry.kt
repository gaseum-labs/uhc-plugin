package com.codeland.uhc.discord.storage

import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Result
import kotlin.reflect.KProperty

abstract class DiscordStorageEntry <T> (val name: String, val default: T) {
	var value: T? = null

	fun isThis(string: String): Boolean {
		val equalsIndex = string.indexOf('=')
		if (equalsIndex == -1) return false
		return string.substring(0, equalsIndex) == name
	}

	fun setData(string: String): Result<Unit> {
		val dataPart = string.split("=", ignoreCase = false, limit = 2).lastOrNull()
			?: return Bad("No information after the equals")

		return when (val r = parse(dataPart)) {
			is Good -> {
				value = r.value
				Good(Unit)
			}
			is Bad -> r.forward()
		}
	}

	abstract fun parse(dataPart: String): Result<T>
	abstract fun format(t: T): String

	operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
		return value
	}

	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
		this.value = value
	}
}

class DiscordStorageEntryLong(name: String, default: Long) : DiscordStorageEntry<Long>(name, default) {
	override fun parse(dataPart: String): Result<Long> {
		return Result.maybe { dataPart.toLong() }
	}

	override fun format(t: Long): String {
		return t.toString()
	}
}

class DiscordStorageEntryHex(name: String, default: Int) : DiscordStorageEntry<Int>(name, default) {
	override fun parse(dataPart: String): Result<Int> {
		return Result.maybe { dataPart.substring(1).toInt(16) }
	}

	override fun format(t: Int): String {
		return "#" + t.toString(16)
	}
}

class DiscordStorageEntryString(name: String, default: String) : DiscordStorageEntry<String>(name, default) {
	override fun parse(dataPart: String): Result<String> {
		val firstQuote = dataPart.indexOf('"')
		if (firstQuote == -1) return Bad("bad string quoting")
		val lastQuote = dataPart.lastIndexOf('"', firstQuote + 1)
		if (lastQuote == -1) return Bad("bad string quoting")

		return Good(dataPart.substring(firstQuote + 1 until lastQuote))
	}

	override fun format(t: String): String {
		return "\"$t\""
	}
}
