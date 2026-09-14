package io.github.twyora.douyinenhancer.config

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class KVProperty<T : Any>(
    private val storage: KVStorage,
    private val key: String,
    private val defValue: T
) : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>) = storage.get(key, defValue)
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = storage.put(key, value)
}

internal fun <T : Any> KVStorage.property(key: String, defValue: T) = KVProperty(this, key, defValue)

