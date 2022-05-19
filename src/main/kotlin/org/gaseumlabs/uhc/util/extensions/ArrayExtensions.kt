package org.gaseumlabs.uhc.util.extensions

object ArrayExtensions {
	fun <T> Array<T>.shuffled(): Array<T> {
		this.shuffle()
		return this
	}
}
