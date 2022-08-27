package org.gaseumlabs.uhc.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

interface Resettable<T> {
	fun reset()
	fun default(): T
}

abstract class UHCProperty<T> : KMutableProperty0<T> {
	abstract override fun getDelegate(): Resettable<T>
}

open class SetResult<S>
class Set<S>(val value: S) : SetResult<S>()
class DontSet<S> : SetResult<S>()

class PropertyGroup(val someUpdated: () -> Unit = {}) {
	val properties = ArrayList<Resettable<*>>()

	fun register(resettable: Resettable<*>) {
		properties.add(resettable)
	}

	fun reset() {
		properties.forEach { it.reset() }
	}

	inline fun <T> delegate(
		initialValue: T,
		crossinline trySet: (set: T) -> SetResult<T> = { Set(it) },
		crossinline onChange: (value: T) -> Unit = {},
	): ReadWriteProperty<Any?, T> =
		object : ReadWriteProperty<Any?, T>, Resettable<T> {
			init { register(this) }
			private var value = initialValue

			fun internalSet(value: T) {
				val filtered = trySet(value)

				if (filtered is Set) {
					onChange(filtered.value)
					this.value = filtered.value
					someUpdated()
				}
			}

			override fun default() = initialValue
			override fun reset() = internalSet(initialValue)

			override fun getValue(thisRef: Any?, property: KProperty<*>) = value

			override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = internalSet(value)
		}
}
