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

	fun <T, R> ArrayList<T>.mapFirstNotNullPrefer(predicate: (T) -> Pair<R?, R?>): R? {
		var fallback: R? = null

		for (element in this) {
			val (optimal, nonOptimal) = predicate(element)

			if (optimal != null) {
				return optimal

			} else if (nonOptimal != null) {
				fallback = nonOptimal
			}
		}

		return fallback
	}
}
