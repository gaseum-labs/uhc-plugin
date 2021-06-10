package com.codeland.uhc.core

class UHCProperty <T> (val default: T, val onSet: (set: T) -> T? = { it }) {
	private var value = default
	private var watchers = ArrayList<() -> Unit>()

	fun get() = value

	fun set(to: T) {
		val filtered = onSet(to)

		if (filtered != null) {
			value = filtered
			watchers.forEach { it() }
		}
	}

	fun unsafeSet(to: T) {
		value = to
		watchers.forEach { it() }
	}

	fun watch(func: () -> Unit) {
		watchers.add(func)
	}

	fun reset() = set(default)
}
