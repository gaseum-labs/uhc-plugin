package com.codeland.uhc.core

class UHCProperty <T> (val default: T, val onSet: (set: T) -> T? = { it }) {
	private var value = default
	private var watcher: () -> Unit = {}

	fun get() = value

	fun set(to: T) {
		val filtered = onSet(to)

		if (filtered != null) {
			value = filtered
			watcher()
		}
	}

	fun unsafeSet(to: T) {
		value = to
		watcher()
	}

	fun watch(func: () -> Unit) {
		watcher = func
	}

	fun reset() = set(default)
}
