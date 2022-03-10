package com.codeland.uhc.reflect

import xyz.jpenilla.reflectionremapper.ReflectionRemapper
import kotlin.reflect.KClass

class UHCReflect<T : Any, G>(clazz: KClass<T>, fieldName: String) {
	private val field = clazz.java.getDeclaredField(
		remapper.remapFieldName(clazz.java, fieldName)
	)

	init {
		field.isAccessible = true
	}

	fun get(instance: T): G {
		return field[instance] as G
	}

	fun set(instance: T, newValue: G) {
		field[instance] = newValue
	}

	companion object {
		val remapper = ReflectionRemapper.forReobfMappingsInPaperJar()
	}
}
