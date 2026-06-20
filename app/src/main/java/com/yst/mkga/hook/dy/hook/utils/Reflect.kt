package com.yst.mkga.hook.dy.hook.utils

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.log.YLog
import com.yst.mkga.hook.dy.hook.DouyinPackage


/**
 * Resolves a type name string into a format directly recognizable by reflection systems like KavaRef.
 *
 * @return Class for primitive type names, or the original String otherwise.
 */
fun String.toClassIfPrimitiveElseString(): Any =
// KavaRef's parameters() accepts Class or String. When given a String,
// it calls Class.forName() internally, which cannot resolve bare primitive
// type names like "int" or "boolean" and throws ClassNotFoundException.
// To work around this, we convert primitive type names to their corresponding
// java.lang.Class instances (e.g. "int" -> Integer.TYPE) so KavaRef hits
// the Class branch directly. Ordinary class names like "java.lang.String"
// are left as raw String, letting KavaRef resolve them via Class.forName()
    // as intended.
    when (this) {
        "boolean" -> Boolean::class.java
        "byte" -> Byte::class.java
        "char" -> Char::class.java
        "short" -> Short::class.java
        "int" -> Int::class.java
        "long" -> Long::class.java
        "float" -> Float::class.java
        "double" -> Double::class.java
        "void" -> Void::class.java
        else -> this
    }

fun Array<String>.toClassIfPrimitiveElseString(): Array<Any> =
    Array(size) {
        this[it].toClassIfPrimitiveElseString()
    }

fun List<String>.toClassIfPrimitiveElseString(): Array<Any> =
    Array(size) {
        this[it].toClassIfPrimitiveElseString()
    }

inline fun <reified T> Any.invokeMethod(method: DouyinPackage.Method, vararg args: Any?): T? {
    val methodResolver = runCatching {
        this.asResolver().firstMethodOrNull {
            name = method.name
            method.parameters?.let { parameters(*it.toClassIfPrimitiveElseString()) }
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this::class.simpleName}.${method.name}(${method.parameters})", it)
    }.getOrNull()

    if (methodResolver == null) {
        YLog.error("Method not found: ${this::class.simpleName}.${method.name}(${method.parameters})")
        return null
    }

    return methodResolver.invoke(*args) as? T
}

inline fun <reified T> Class<*>.invokeStaticMethod(method: DouyinPackage.Method, vararg args: Any?): T? {
    val methodResolver = runCatching {
        this.resolve().firstMethodOrNull {
            name = method.name
            method.parameters?.let { parameters(*it.toClassIfPrimitiveElseString()) }
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this.simpleName}.${method.name}(${method.parameters})", it)
    }.getOrNull()

    if (methodResolver == null) {
        YLog.error("Method not found: ${this.simpleName}.${method.name}(${method.parameters})")
        return null
    }

    return methodResolver.invoke(*args) as? T
}

inline fun <reified T> Any.getField(field: DouyinPackage.Field): T? {
    val fieldResolver = runCatching {
        this.asResolver().firstField {
            name = field.name
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this::class.java.simpleName}.${field.name}", it)
    }.getOrNull()

    if (fieldResolver == null) {
        YLog.error("Field not found: ${this::class.java.simpleName}.${field.name}")
        return null
    }

    return fieldResolver.get() as? T
}

fun <T> Any.setField(field: DouyinPackage.Field, value: T?) {
    val fieldResolver = runCatching {
        this.asResolver().firstField {
            name = field.name
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this::class.java.simpleName}.${field.name}", it)
    }.getOrNull()

    if (fieldResolver == null) {
        YLog.error("Field not found: ${this::class.java.simpleName}.${field.name}")
        return
    }

    fieldResolver.set(value)
}

inline fun <reified T> Class<*>.getStaticField(field: DouyinPackage.Field): T? {
    val fieldResolver = runCatching {
        this.resolve().firstField {
            name = field.name
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this.simpleName}.${field.name}", it)
    }.getOrNull()

    if (fieldResolver == null) {
        YLog.error("Field not found: ${this.simpleName}.${field.name}")
        return null
    }

    return fieldResolver.get() as? T
}

fun <T> Class<*>.setStaticField(field: DouyinPackage.Field, value: T?) {
    val fieldResolver = runCatching {
        this.resolve().firstField {
            name = field.name
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this.simpleName}.${field.name}", it)
    }.getOrNull()

    if (fieldResolver == null) {
        YLog.error("Field not found: ${this.simpleName}.${field.name}")
        return
    }

    fieldResolver.set(value)
}