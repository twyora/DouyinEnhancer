package com.yst.mkga.hook.dy.hook.utils


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