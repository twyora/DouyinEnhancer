package io.github.twyora.douyinenhancer.hook.utils

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.resolver.FieldResolver
import com.highcapable.kavaref.resolver.MethodResolver
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.DouyinPackage

/**
 * Resolves a type name string into a format directly recognizable by reflection systems like KavaRef.
 *
 * @return Class for primitive type names, or the original String otherwise.
 */
fun String.toClassIfPrimitiveElseString(): Any = when (this) {
    // KavaRef's parameters() accepts Class or String. When given a String,
    // it calls Class.forName() internally, which cannot resolve bare primitive
    // type names like "int" or "boolean" and throws ClassNotFoundException.
    // To work around this, we convert primitive type names to their corresponding
    // java.lang.Class instances (e.g. "int" -> Integer.TYPE) so KavaRef hits
    // the Class branch directly. Ordinary class names like "java.lang.String"
    // are left as raw String, letting KavaRef resolve them via Class.forName()
    // as intended.
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

fun Array<String>.toClassIfPrimitiveElseString(): Array<Any> = Array(size) {
    this[it].toClassIfPrimitiveElseString()
}

fun List<String>.toClassIfPrimitiveElseString(): Array<Any> = Array(size) {
    this[it].toClassIfPrimitiveElseString()
}

fun Any.resolveMethod(method: DouyinPackage.Method): MethodResolver<*>? {
    if (method.name.isNullOrBlank()) {
        YLog.error("Cannot determine which method to resolve on ${this::class.simpleName}, name is null or blank")
        return null
    }
    return runCatching {
        this.asResolver().firstMethodOrNull {
            name = method.name
            method.parameters?.let {
                parameters(*it.toClassIfPrimitiveElseString())
            }
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this::class.simpleName}.${method.name}(${method.parameters})")
    }.getOrNull()
        .also {
            if (it == null) {
                YLog.error("Method not found: ${this::class.simpleName}.${method.name}(${method.parameters})")
            }
        }
}

fun Any.resolveField(field: DouyinPackage.Field): FieldResolver<*>? {
    if (field.name.isNullOrBlank()) {
        YLog.error("Cannot determine which field to resolve on ${this::class.simpleName}, name is null or blank")
        return null
    }
    return runCatching {
        this.asResolver().firstFieldOrNull {
            name = field.name
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this::class.simpleName}.${field.name}")
    }.getOrNull()
        .also {
            if (it == null) {
                YLog.error("Field not found: ${this::class.simpleName}.${field.name}")
            }
        }
}

fun Class<*>.resolveMethod(method: DouyinPackage.Method): MethodResolver<*>? {
    if (method.name.isNullOrBlank()) {
        YLog.error("Cannot determine which method to resolve on ${this.simpleName}, name is null or blank")
        return null
    }
    return runCatching {
        this.resolve().firstMethodOrNull {
            name = method.name
            method.parameters?.let {
                parameters(*it.toClassIfPrimitiveElseString())
            }
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this.simpleName}.${method.name}(${method.parameters})")
    }.getOrNull()
        .also {
            if (it == null) {
                YLog.error("Method not found: ${this.simpleName}.${method.name}(${method.parameters})")
            }
        }
}

fun Class<*>.resolveField(field: DouyinPackage.Field): FieldResolver<*>? {
    if (field.name.isNullOrBlank()) {
        YLog.error("Cannot determine which field to resolve on ${this.simpleName}, name is null or blank")
        return null
    }
    return runCatching {
        this.resolve().firstFieldOrNull {
            name = field.name
            superclass()
        }
    }.onFailure {
        YLog.error("Resolve failed: ${this.simpleName}.${field.name}")
    }.getOrNull()
        .also {
            if (it == null) {
                YLog.error("Field not found: ${this.simpleName}.${field.name}")
            }
        }
}

inline fun <reified T> Any.invokeMethod(method: DouyinPackage.Method, vararg args: Any?): T? =
    this.resolveMethod(method)?.invoke(*args) as? T

inline fun <reified T> Class<*>.invokeStaticMethod(method: DouyinPackage.Method, vararg args: Any?): T? =
    this.resolveMethod(method)?.invoke(*args) as? T

inline fun <reified T> Any.getField(field: DouyinPackage.Field): T? = this.resolveField(field)?.get() as? T

fun <T> Any.setField(field: DouyinPackage.Field, value: T?) {
    this.resolveField(field)?.set(value)
}

inline fun <reified T> Class<*>.getStaticField(field: DouyinPackage.Field): T? = this.resolveField(field)?.get() as? T

fun <T> Class<*>.setStaticField(field: DouyinPackage.Field, value: T?) {
    this.resolveField(field)?.set(value)
}
