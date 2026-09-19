package io.github.twyora.douyinenhancer.config.kvstorage

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class KVReadWriteDelegate<T : Any>(
    private val storage: IKVStorage,
    private val key: String,
    private val defValue: T
) : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>) = storage.get(key, defValue)
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = storage.put(key, value)
}

fun <T : Any> IKVStorage.readWriteConfig(key: String, defValue: T) = KVReadWriteDelegate(this, key, defValue)

