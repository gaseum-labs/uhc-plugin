package com.codeland.uhc.util

import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

object ReflectionUtil {

    public inline fun <reified T> KClass<out Any>.getFieldOfType(): KProperty1<out Any, *> {
        this.declaredMemberProperties.forEach { field ->
            if (field.returnType is T) {
                field.isAccessible = true
                return field
            }
        }
        throw NoSuchElementException("No field on ${this.simpleName} of type ${T::class.qualifiedName}")
    }

}