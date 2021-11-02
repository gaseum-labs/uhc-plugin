package com.codeland.uhc.util

class PreMixedResult {
	val errors = ArrayList<String>()

	fun error(error: String?) {
		errors.add(error ?: "Unknown Error")
	}
}

class MixedResult<T>(val value: T, preMixedResult: PreMixedResult) {
	val errors = preMixedResult.errors

	operator fun component1(): T {
		return value
	}

	operator fun component2(): ArrayList<String> {
		return errors
	}
}
