package com.codeland.uhc.util.extensions

object ArrayListExtensions {
	fun <T> ArrayList<T>.removeFirst(predicate: (T) -> Boolean): T? {
		/* find the first and only element that matched predicate */
		for (i in this.indices) {
			if (predicate(this[i])) {
				return this.removeAt(i)
			}
		}

		return null
	}
}
