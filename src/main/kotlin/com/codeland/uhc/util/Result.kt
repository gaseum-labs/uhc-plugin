package com.codeland.uhc.util

/**
 * To be used for IO parsing, where user input can be bad
 *
 * usage:
 *
 * when (val s = resultFunc()) {
 *     is Good -> {}
 *     is Bad -> {}
 * }
 */
sealed class Result<T> {
	companion object {
		fun <T> maybe(throwing: () -> T): Result<T> {
			return try {
				Good(throwing())
			} catch (ex: Exception) {
				Bad(ex.message)
			}
		}
	}
}

class Good<T>(val value: T) : Result<T>()

class Bad<T>(error: String?) : Result<T>() {
	val error = error ?: "Unknown Error"
	fun <X> forward(): Bad<X> = Bad(error)
}
