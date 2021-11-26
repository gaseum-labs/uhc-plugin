package com.codeland.uhc.discord.storage

import com.codeland.uhc.util.*
import com.codeland.uhc.util.extensions.ArrayListExtensions.removeFirst
import java.util.*
import kotlin.reflect.KProperty

data class Param(val name: String?, val value: String)

abstract class StorageEntry<T>(val name: String?) {
	var value: T? = null

	fun setValue(dataPart: String): Result<Unit> {
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

	companion object {
		fun massAssign(entries: ArrayList<StorageEntry<*>>, params: ArrayList<Param>): ArrayList<String> {
			val map = HashMap<StorageEntry<*>, Param>()
			val errors = ArrayList<String>()

			val (namedParams, unnamedParams) = params.partition { it.name != null }

			namedParams.forEach { param ->
				val entry = entries.removeFirst { it.name == param.name }
				if (entry != null) {
					map[entry] = param
				} else {
					errors.add("Unknown parameter ${param.name}")
				}
			}

			unnamedParams.forEach { param ->
				if (entries.isNotEmpty()) {
					map[entries.removeFirst()] = param
				} else {
					errors.add("Unnecessary param ${param.value}")
				}
			}

			map.forEach { (entry, param) ->
				val r = entry.setValue(param.value)
				if (r is Bad) errors.add("Error when parsing ${entry.name}: ${r.error}")
			}

			return errors
		}

		fun getParams(string: String): ArrayList<Param> {
			val params = ArrayList<String>()
			val builder = StringBuilder()

			var inQuote = false
			var escaping = false
			for (c in string) {
				when (c) {
					' ' -> {
						if (inQuote) {
							builder.append(c)
						} else {
							if (builder.isNotEmpty()) {
								params.add(builder.toString())
								builder.clear()
							}
						}
						escaping = false
					}
					'\\' -> {
						escaping = if (escaping) {
							builder.append(c)
							false
						} else {
							true
						}
					}
					'"' -> {
						if (escaping) {
							builder.append(c)
							escaping = false
						} else inQuote = !inQuote
					}
					else -> {
						builder.append(c)
						escaping = false
					}
				}
			}
			if (builder.isNotEmpty()) params.add(builder.toString())

			return params.map { param ->
				val equalsIndex = param.indexOf('=')
				if (equalsIndex != -1) {
					Param(param.substring(0 until equalsIndex), param.substring(equalsIndex + 1 until param.length))
				} else {
					Param(null, param)
				}
			} as ArrayList<Param>
		}
	}
}

class StorageEntryLong(name: String) : StorageEntry<Long>(name) {
	override fun parse(dataPart: String): Result<Long> {
		return Result.maybe { dataPart.toLong() }
	}

	override fun format(t: Long): String {
		return t.toString()
	}
}

class StorageEntryInt(name: String) : StorageEntry<Int>(name) {
	override fun parse(dataPart: String): Result<Int> {
		return Result.maybe { dataPart.toInt() }
	}

	override fun format(t: Int): String {
		return t.toString()
	}
}

class StorageEntryHex(name: String) : StorageEntry<Int>(name) {
	override fun parse(dataPart: String): Result<Int> {
		return Result.maybe { dataPart.substring(1).toInt(16) }
	}

	override fun format(t: Int): String {
		return "#" + t.toString(16)
	}
}

class StorageEntryString(name: String) : StorageEntry<String>(name) {
	override fun parse(dataPart: String): Result<String> {
		return Good(dataPart)
	}

	override fun format(t: String): String {
		return "\"$t\""
	}
}

class StorageEntryUuid(name: String) : StorageEntry<UUID>(name) {
	override fun parse(dataPart: String): Result<UUID> {
		return Result.maybe { UUID.fromString(dataPart) }
	}

	override fun format(t: UUID): String {
		return t.toString()
	}
}

fun main() {
	val seasonNumberEntry = StorageEntryInt("number")
	val colorEntry = StorageEntryHex("color")
	val championColorEntry = StorageEntryHex("championColor")
	val championEntry = StorageEntryUuid("champion")

	val errors = StorageEntry.massAssign(
		arrayListOf(
			seasonNumberEntry,
			colorEntry,
			championColorEntry,
			championEntry
		),
		StorageEntry.getParams("number=7 champion=504a4dfa-2ec6-40e4-80d2-46b92c9f3164")
	)

	if (errors.isNotEmpty()) errors.forEach(::println)

	println(seasonNumberEntry.value)
	println(colorEntry.value)
	println(championColorEntry.value)
	println(championEntry.value)
}
