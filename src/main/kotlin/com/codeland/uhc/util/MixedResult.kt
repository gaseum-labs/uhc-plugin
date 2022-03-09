package com.codeland.uhc.util

class PreMixedResult {
	val errors = ArrayList<String>()

	fun error(error: String?) {
		errors.add(error ?: "Unknown Error")
	}

	fun <T> complete(value: T): MixedResult<T> {
		return MixedResult(value, errors)
	}
}

class MixedResult<T>(val value: T, val errors: ArrayList<String>) {
	operator fun component1(): T {
		return value
	}

	operator fun component2(): ArrayList<String> {
		return errors
	}
}
