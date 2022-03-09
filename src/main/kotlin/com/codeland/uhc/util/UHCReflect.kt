package com.codeland.uhc.util

import kotlin.reflect.KClass

class UHCReflect<T : Any, G>(clazz: KClass<T>, fieldName: String) {
	private val field = clazz.java.getDeclaredField(fieldName)

	init {
		field.isAccessible = true
	}

	fun get(instance: T): G {
		return field[instance] as G
	}

	fun set(instance: T, newValue: G) {
		field[instance] = newValue
	}
}
