package com.codeland.uhc.reflect

import com.codeland.uhc.reflect.UHCReflect.Companion.remapper
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

class UHCMethodReflect<T : Any, R>(clazz: KClass<T>, methodName: String, vararg paramTypes: Class<*>) {
	private val method = clazz.java.getDeclaredMethod(
		remapper.remapMethodName(clazz.java, methodName, *paramTypes)
	)

	init {
		method.isAccessible = true
	}

	fun call(instance: T, vararg arguments: Any): R {
		return method.invoke(instance, *arguments) as R
	}
}
