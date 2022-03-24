package org.gaseumlabs.uhc.util.reflect

import kotlin.reflect.KClass

class UHCMethodReflect<T : Any, R>(clazz: KClass<T>, methodName: String, vararg paramTypes: Class<*>) {
    private val method = clazz.java.getDeclaredMethod(
        UHCReflect.remapper.remapMethodName(clazz.java, methodName, *paramTypes)
    )

    init {
        method.isAccessible = true
    }

    fun call(instance: T, vararg arguments: Any): R {
        return method.invoke(instance, *arguments) as R
    }
}
