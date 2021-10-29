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
sealed class Result<T>

class Good<T>(val value: T): Result<T>()

class Bad<T>(val error: String): Result<T>() {
	fun <X> forward(): Bad<X> = Bad(error)
}
