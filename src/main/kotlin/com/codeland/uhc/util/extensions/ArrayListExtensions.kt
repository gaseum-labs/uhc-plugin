package com.codeland.uhc.util.extensions

object ArrayListExtensions {
	fun <T> ArrayList<T>.removeFirst(predicate: (T) -> Boolean): ArrayList<T> {
		/* find the first and only element that matched predicate */
		for (i in this.indices) {
			if (predicate(this[i])) {
				/* shift all elements after it back by 1 */
				for (j in i until this.lastIndex) {
					this[j] = this[j + 1]
				}
				this.removeAt(this.lastIndex)

				return this
			}
		}

		return this
	}
}
