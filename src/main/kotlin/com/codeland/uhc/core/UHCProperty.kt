package com.codeland.uhc.core

class UHCProperty <T> (val default: T) {
	private var value = default
	private var watcher: () -> Unit = {}

	fun get () = value

	fun set (to: T) {
		value = to
		watcher()
	}

	fun watch(func: () -> Unit) {
		watcher = func
	}

	fun reset() {
		value = default
	}
}
