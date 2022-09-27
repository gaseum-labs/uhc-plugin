package org.gaseumlabs.uhc.util

inline fun <reified T>createStaticMap(vararg elements: T, toKey: (T) -> Int): StaticMap<T> {
	val length = (elements.maxOfOrNull(toKey) ?: -1) + 1
	return StaticMap(Array(length) { i ->
		elements.find { toKey(it) == i }
	})
}

class StaticMap<T>(val array: Array<T?>) {
	operator fun get(id: Int): T = array[id]!!

	fun size() = array.size

	inline fun <reified R> map(mapper: (T) -> R): StaticMap<R> {
		return StaticMap(Array(size()) { i ->
			val element = array[i]
			if (element == null) null else mapper(element)
		})
	}

	fun indices() = array.indices.filter { array[it] != null }
}
