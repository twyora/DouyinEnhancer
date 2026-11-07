package io.github.twyora.douyinenhancer.utils.reflect

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.ArrayClass
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.toClass as KavaRefExt_toClass
import com.highcapable.kavaref.resolver.FieldResolver
import com.highcapable.kavaref.resolver.MethodResolver
import com.highcapable.yukihookapi.hook.log.YLog
import kotlin.reflect.KClass

data class Field(val name: String?)

data class Method(val name: String?, val parameters: List<String>?)

@JvmOverloads
fun String.toClass(loader: ClassLoader? = null, initialize: Boolean = false): Class<*> {
    // KavaRef's toClass doesn't support primitive types and JVM descriptors, manually handle them here
    return when (this) {
        "I", "int" -> classOf<Int>()

        "Z", "boolean" -> classOf<Boolean>()

        "B", "byte" -> classOf<Byte>()

        "C", "char" -> classOf<Char>()

        "S", "short" -> classOf<Short>()

        "J", "long" -> classOf<Long>()

        "F", "float" -> classOf<Float>()

        "D", "double" -> classOf<Double>()

        "V", "void" -> Void.TYPE

        else -> {
            if (this.startsWith("L") && this.endsWith(";")) {
                this.substring(1, this.length - 1).replace("/", ".").KavaRefExt_toClass(loader, initialize)
            } else if (this.endsWith("[]")) {
                val dimensionCount = Regex("(\\[])+$").find(this)?.value?.length?.div(2) ?: 0
                var arrayClass = this.substring(0, this.length - dimensionCount * 2).toClass(loader, initialize)
                repeat(dimensionCount) {
                    arrayClass = ArrayClass(arrayClass) as Class<*>
                }
                arrayClass
            } else {
                this.KavaRefExt_toClass(loader, initialize)
            }
        }
    }
}

@JvmOverloads
fun String.toClassOrNull(loader: ClassLoader? = null, initialize: Boolean = false) = runCatching {
    this.toClass(loader, initialize)
}.getOrNull()

@JvmOverloads
fun Iterable<String>.toClasses(loader: ClassLoader? = null, initialize: Boolean = false): Array<Class<*>> = this.map {
    it.toClass(loader, initialize)
}.toTypedArray()

@JvmOverloads
fun Iterable<String>.toClassesOrNull(loader: ClassLoader? = null, initialize: Boolean = false): Array<Class<*>>? = runCatching {
    this.toClasses(loader, initialize)
}.getOrNull()

@JvmOverloads
fun Array<String>.toClasses(loader: ClassLoader? = null, initialize: Boolean = false): Array<Class<*>> = Array(size) {
    get(it).toClass(loader, initialize)
}

@JvmOverloads
fun Array<String>.toClassesOrNull(loader: ClassLoader? = null, initialize: Boolean = false): Array<Class<*>>? = runCatching {
    this.toClasses(loader, initialize)
}.getOrNull()

fun <T : Any> Class<T>.resolveMethod(method: Method): MethodResolver<T> {
    if (method.name.isNullOrBlank()) {
        throw IllegalArgumentException("cannot determine which method to resolve on ${this.name}, name is null or blank")
    }
    return this.resolve().firstMethod {
        name = method.name
        method.parameters?.let {
            if (it.isEmpty()) {
                emptyParameters()
            } else {
                parameters(*it.toClasses(this@resolveMethod.classLoader))
            }
        }
        superclass()
    }
}

fun <T : Any> Class<T>.resolveMethodOrNull(method: Method): MethodResolver<T>? = runCatching {
    this.resolveMethod(method)
}.onFailure {
    YLog.error("resolve failed: ${this.name}.${method.name}(${method.parameters})", it)
}.getOrNull()

fun <T : Any> Class<T>.resolveField(field: Field): FieldResolver<T> {
    if (field.name.isNullOrBlank()) {
        throw IllegalArgumentException("cannot determine which field to resolve on ${this.name}, name is null or blank")
    }
    return this.resolve().firstField {
        name = field.name
        superclass()
    }
}

fun <T : Any> Class<T>.resolveFieldOrNull(field: Field): FieldResolver<T>? = runCatching {
    this.resolveField(field)
}.onFailure {
    YLog.error("resolve failed: ${this.name}.${field.name}", it)
}.getOrNull()

fun <T : Any> KClass<T>.resolveMethod(method: Method): MethodResolver<T> = this.java.resolveMethod(method)

fun <T : Any> KClass<T>.resolveMethodOrNull(method: Method): MethodResolver<T>? = this.java.resolveMethodOrNull(method)

fun <T : Any> KClass<T>.resolveField(field: Field): FieldResolver<T> = this.java.resolveField(field)

fun <T : Any> KClass<T>.resolveFieldOrNull(field: Field): FieldResolver<T>? = this.java.resolveFieldOrNull(field)

@JvmName("invokeStaticMethodTyped")
inline fun <reified T> Class<*>.invokeStaticMethod(method: Method, vararg args: Any?): T? = invokeStaticMethod(method, *args) as T?

fun Class<*>.invokeStaticMethod(method: Method, vararg args: Any?): Any? = this.resolveMethod(method).invoke(*args)

@JvmName("invokeStaticMethodOrNullTyped")
inline fun <reified T> Class<*>.invokeStaticMethodOrNull(method: Method, vararg args: Any?): T? =
    invokeStaticMethodOrNull(method, *args) as? T?

fun Class<*>.invokeStaticMethodOrNull(method: Method, vararg args: Any?): Any? = runCatching {
    invokeStaticMethod(method, *args)
}.onFailure {
    YLog.error("failed to invoke static method: ${this.name}.${method.name}(${method.parameters})", it)
}.getOrNull()

@JvmName("invokeStaticMethodTyped")
inline fun <reified T> KClass<*>.invokeStaticMethod(method: Method, vararg args: Any?): T? = invokeStaticMethod(method, *args) as T?

fun KClass<*>.invokeStaticMethod(method: Method, vararg args: Any?): Any? = this.java.invokeStaticMethod(method, *args)

@JvmName("invokeStaticMethodOrNullTyped")
inline fun <reified T> KClass<*>.invokeStaticMethodOrNull(method: Method, vararg args: Any?): T? =
    invokeStaticMethodOrNull(method, *args) as? T?

fun KClass<*>.invokeStaticMethodOrNull(method: Method, vararg args: Any?): Any? = runCatching {
    invokeStaticMethod(method, *args)
}.onFailure {
    YLog.error("failed to invoke static method: ${this.java.name}.${method.name}(${method.parameters})", it)
}.getOrNull()

@JvmName("getStaticFieldTyped")
inline fun <reified T> Class<*>.getStaticField(field: Field): T? = getStaticField(field) as T?

fun Class<*>.getStaticField(field: Field): Any? = this.resolveField(field).get()

@JvmName("getStaticFieldOrNullTyped")
inline fun <reified T> Class<*>.getStaticFieldOrNull(field: Field): T? = getStaticFieldOrNull(field) as? T?

fun Class<*>.getStaticFieldOrNull(field: Field): Any? = runCatching {
    getStaticField(field)
}.onFailure {
    YLog.error("failed to get static field: ${this.name}.${field.name}", it)
}.getOrNull()

@JvmName("getStaticFieldTyped")
inline fun <reified T> KClass<*>.getStaticField(field: Field): T? = getStaticField(field) as T?

fun KClass<*>.getStaticField(field: Field): Any? = this.java.getStaticField(field)

@JvmName("getStaticFieldOrNullTyped")
inline fun <reified T> KClass<*>.getStaticFieldOrNull(field: Field): T? = getStaticFieldOrNull(field) as? T?

fun KClass<*>.getStaticFieldOrNull(field: Field): Any? = runCatching {
    getStaticField(field)
}.onFailure {
    YLog.error("failed to get static field: ${this.java.name}.${field.name}", it)
}.getOrNull()

fun Class<*>.setStaticField(field: Field, value: Any?) = this.resolveField(field).set(value)

fun Class<*>.setStaticFieldQuietly(field: Field, value: Any?) = runCatching {
    setStaticField(field, value)
}.onFailure {
    YLog.error("failed to set static field: ${this.name}.${field.name}", it)
}.getOrNull()

fun KClass<*>.setStaticField(field: Field, value: Any?) = this.java.setStaticField(field, value)

fun KClass<*>.setStaticFieldQuietly(field: Field, value: Any?) = runCatching {
    setStaticField(field, value)
}.onFailure {
    YLog.error("failed to set static field: ${this.java.name}.${field.name}", it)
}.getOrNull()

fun <T : Any> T.resolveMethod(method: Method): MethodResolver<T> {
    @Suppress("UNCHECKED_CAST")
    val thisClass = this::class.java as Class<T>
    return thisClass.resolveMethod(method).of(this)
}

fun <T : Any> T.resolveMethodOrNull(method: Method): MethodResolver<T>? {
    @Suppress("UNCHECKED_CAST")
    val thisClass = this::class.java as Class<T>
    return thisClass.resolveMethodOrNull(method)?.of(this)
}

fun <T : Any> T.resolveField(field: Field): FieldResolver<T> {
    @Suppress("UNCHECKED_CAST")
    val thisClass = this::class.java as Class<T>
    return thisClass.resolveField(field).of(this)
}

fun <T : Any> T.resolveFieldOrNull(field: Field): FieldResolver<T>? {
    @Suppress("UNCHECKED_CAST")
    val thisClass = this::class.java as Class<T>
    return thisClass.resolveFieldOrNull(field)?.of(this)
}

@JvmName("invokeMethodTyped")
inline fun <reified T> Any.invokeMethod(method: Method, vararg args: Any?): T? = invokeMethod(method, *args) as T?

fun Any.invokeMethod(method: Method, vararg args: Any?): Any? = this.resolveMethod(method).invoke(*args)

@JvmName("invokeMethodOrNullTyped")
inline fun <reified T> Any.invokeMethodOrNull(method: Method, vararg args: Any?): T? = invokeMethodOrNull(method, *args) as? T?

fun Any.invokeMethodOrNull(method: Method, vararg args: Any?): Any? = runCatching {
    invokeMethod(method, *args)
}.onFailure {
    YLog.error("failed to invoke method: ${this.javaClass.name}.${method.name}(${method.parameters})", it)
}.getOrNull()

fun Any.invokeMethodOnly(method: Method, vararg args: Any?) {
    this.invokeMethod(method, *args)
}

fun Any.invokeMethodOnlyQuietly(method: Method, vararg args: Any?) {
    runCatching {
        this.invokeMethodOnly(method, *args)
    }
}

@JvmName("getFieldTyped")
inline fun <reified T> Any.getField(field: Field): T? = getField(field) as T?

fun Any.getField(field: Field): Any? = this.resolveField(field).get()

@JvmName("getFieldOrNullTyped")
inline fun <reified T> Any.getFieldOrNull(field: Field): T? = getFieldOrNull(field) as? T?

fun Any.getFieldOrNull(field: Field): Any? = runCatching {
    getField(field)
}.onFailure {
    YLog.error("failed to get field: ${this.javaClass.name}.${field.name}", it)
}.getOrNull()

fun Any.setField(field: Field, value: Any?) = this.resolveField(field).set(value)

fun Any.setFieldQuietly(field: Field, value: Any?) = runCatching {
    setField(field, value)
}.onFailure {
    YLog.error("failed to set field: ${this.javaClass.name}.${field.name}", it)
}.getOrNull()
