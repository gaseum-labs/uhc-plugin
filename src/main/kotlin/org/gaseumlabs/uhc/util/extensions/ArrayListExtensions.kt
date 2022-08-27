package org.gaseumlabs.uhc.util.extensions

object ArrayListExtensions {
	inline fun <T> ArrayList<T>.removeFirst(predicate: (T) -> Boolean): T? {
		/* find the first and only element that matched predicate */
		for (i in this.indices) {
			if (predicate(this[i])) {
				return this.removeAt(i)
			}
		}

		return null
	}

	inline fun <T, R> ArrayList<T>.mapFirstNotNullPrefer(predicate: (T) -> Pair<R?, R?>): R? {
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

	inline fun <T, R> ArrayList<T>.mapUHC(transform: (T) -> R): ArrayList<R> {
		val transformed = ArrayList<R>(this.size)

		for (element in this) {
			transformed.add(transform(element))
		}

		return transformed
	}

	inline fun <T, R> ArrayList<T>.mapNotNullUHC(transform: (T) -> R?): ArrayList<R> {
		val transformed = ArrayList<R>(this.size)

		for (element in this) {
			val potential = transform(element)
			if (potential != null) {
				transformed.add(potential)
			}
		}

		return transformed
	}

	inline fun <T, R> ArrayList<T>.inPlaceReplace(other: ArrayList<R>, transform: (R) -> T) {
		this.ensureCapacity(other.size)

		for (i in other.indices) {
			if (i < this.size) {
				this[i] = transform(other[i])
			} else {
				this.add(transform(other[i]))
			}
		}

		/* trim elements off the end */
		while (this.size > other.size) {
			this.removeAt(this.size - 1)
		}
	}
}
